package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.dto.SinistroMobileDtos.*;
import com.mais.seguradora.enummerated.*;
import com.mais.seguradora.exception.ComplementoMobileException;
import com.mais.seguradora.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SinistroMobileService {
    private final SinistroRepository sinistros;
    private final ContratacaoRepository contratos;
    private final PagamentoRepository pagamentos;
    private final ClienteRepository clientes;
    private final AdminRepository admins;
    public SinistroMobileService(SinistroRepository s, ContratacaoRepository c, PagamentoRepository p, ClienteRepository cl, AdminRepository a) {
        sinistros=s; contratos=c; pagamentos=p; clientes=cl; admins=a;
    }
    public List<Opcao> opcoes(Integer clienteId) {
        cliente(clienteId);
        List<Contratacao> proprios=contratos.findByClienteId(clienteId).stream().filter(c -> pertence(c,clienteId)).toList();
        Map<Integer,Apolice> produtos=new TreeMap<>();
        proprios.forEach(c -> { if(c.getApolice()!=null && c.getApolice().getId()!=null) produtos.put(c.getApolice().getId(),c.getApolice()); });
        return produtos.values().stream().map(a -> {
            String motivo=impedimento(clienteId,a.getId(),proprios,false);
            return new Opcao(a.getId(),tipo(a),a.getNumeroApolice(),motivo==null,motivo);
        }).toList();
    }
    public List<Resumo> listarCliente(Integer id) {
        cliente(id);
        return ordenar(sinistros.findByCliente_IdOrderByIdDesc(id)).stream().filter(s -> Objects.equals(s.getCliente().getId(),id)).map(s -> resumo(s,false)).toList();
    }
    public Resumo detalheCliente(Integer clienteId,Integer id) {
        cliente(clienteId);
        Sinistro s= id==null||id<=0 ? null : sinistros.findByIdAndCliente_Id(id,clienteId).orElse(null);
        if(!integro(s)||!Objects.equals(s.getCliente().getId(),clienteId)) throw ausente();
        return resumo(s,false);
    }
    public List<Resumo> admin(Integer id) { adminValido(id); return ordenar(sinistros.findAll()).stream().map(s -> resumo(s,true)).toList(); }
    public Resumo detalheAdmin(Integer adminId,Integer id) {
        adminValido(adminId);
        Sinistro s=id==null||id<=0?null:sinistros.findById(id).orElse(null);
        if(!integro(s)) throw ausente();
        return resumo(s,true);
    }
    @Transactional
    public Resumo abrir(Integer clienteId,Abertura pedido) {
        if(clienteId==null||clienteId<=0) throw sessao();
        // Serializa aberturas por proprietário; depois contratações e pagamentos, sempre por ID.
        Cliente cliente=clientes.findForSolicitacao(clienteId).orElseThrow(SinistroMobileService::sessao);
        if(!Objects.equals(cliente.getId(),clienteId)) throw sessao();
        if(pedido==null||pedido.apoliceId()==null||pedido.apoliceId()<=0||pedido.descricao()==null
                ||pedido.descricao().strip().length()<10||pedido.descricao().strip().length()>2000||!valor(pedido.valorPrejuizo()))
            throw new ComplementoMobileException(400,"Confira a descrição e o prejuízo estimado.");
        List<Contratacao> proprios=contratos.findForSinistro(clienteId,pedido.apoliceId()).stream()
                .filter(c -> pertence(c,clienteId)&&c.getApolice()!=null&&Objects.equals(c.getApolice().getId(),pedido.apoliceId())).toList();
        if(proprios.isEmpty()) throw new ComplementoMobileException(404,"Produto não encontrado.");
        String motivo=impedimento(clienteId,pedido.apoliceId(),proprios,true);
        if(motivo!=null) throw new ComplementoMobileException(409,motivo);
        Contratacao c=proprios.stream().filter(x -> x.getStatus()==StatusContratacao.APOLICE_EMITIDA).findFirst().orElseThrow();
        Sinistro s=new Sinistro(); s.setCliente(cliente);s.setApolice(c.getApolice());s.setDescricao(pedido.descricao().strip());
        s.setValorPrejuizo(pedido.valorPrejuizo());s.setDataSinsitro(LocalDate.now());s.setStatus(StatusSinistro.ANALISANDO);
        // Mesmo modelo/estado da abertura legada, sem disparar email ou decisão de análise.
        return resumo(sinistros.save(s),false);
    }
    private String impedimento(Integer clienteId,Integer produtoId,List<Contratacao> lista,boolean lock) {
        List<Contratacao> emitidas=lista.stream().filter(c -> pertence(c,clienteId)&&c.getApolice()!=null
                &&Objects.equals(c.getApolice().getId(),produtoId)&&c.getStatus()==StatusContratacao.APOLICE_EMITIDA).toList();
        if(emitidas.isEmpty()) return "Registro disponível somente para apólice emitida.";
        if(emitidas.size()!=1) return "Há mais de uma apólice emitida para este produto. Solicite a conferência dos dados.";
        Contratacao contrato=emitidas.getFirst();
        List<Pagamento> registros=lock?pagamentos.findForMobileOperation(contrato.getId()):pagamentos.findByContratacao_IdOrderByIdAsc(contrato.getId());
        boolean pago=registros.stream().anyMatch(p -> p!=null&&p.getContratacao()!=null&&Objects.equals(p.getContratacao().getId(),contrato.getId())
                &&pertence(p.getContratacao(),clienteId)&&p.getStatus()==StatusPagamento.APROVADA);
        if(!pago) return "É necessário pagamento aprovado para registrar o sinistro.";
        if(sinistros.existsByCliente_IdAndApolice_IdAndStatus(clienteId,produtoId,StatusSinistro.ANALISANDO)) return "Já existe um sinistro em análise para este produto.";
        return null;
    }
    private void cliente(Integer id) { if(id==null||id<=0||!clientes.existsById(id)) throw sessao(); }
    private void adminValido(Integer id) { if(id==null||id<=0||!admins.existsById(id)) throw sessao(); }
    private static boolean pertence(Contratacao c,Integer id) { return c!=null&&c.getId()!=null&&c.getCliente()!=null&&Objects.equals(c.getCliente().getId(),id); }
    private static boolean integro(Sinistro s) { return s!=null&&s.getId()!=null&&s.getId()>0&&s.getCliente()!=null&&s.getCliente().getId()!=null&&s.getApolice()!=null&&s.getApolice().getId()!=null; }
    private static List<Sinistro> ordenar(List<Sinistro> lista) { return lista.stream().filter(SinistroMobileService::integro).sorted(Comparator.comparing(Sinistro::getId).reversed()).toList(); }
    private static String tipo(Apolice a) { return a.getTipoApolice()==null||a.getTipoApolice().isBlank()?"Seguro não informado":a.getTipoApolice(); }
    private static Resumo resumo(Sinistro s,boolean admin) { return new Resumo(s.getId(),s.getApolice().getId(),tipo(s.getApolice()),s.getApolice().getNumeroApolice(),s.getDescricao(),s.getDataSinsitro(),s.getValorPrejuizo(),s.getStatus()==null?"NAO_INFORMADO":s.getStatus().name(),admin?s.getCliente().getNome():null); }
    private static boolean valor(BigDecimal v) { return v!=null&&v.compareTo(new BigDecimal("0.01"))>=0&&v.compareTo(new BigDecimal("99999999.99"))<=0&&Math.max(0,v.stripTrailingZeros().scale())<=2; }
    private static ComplementoMobileException ausente() { return new ComplementoMobileException(404,"Sinistro não encontrado."); }
    private static ComplementoMobileException sessao() { return new ComplementoMobileException(401,"Entre novamente para continuar."); }
}
