package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.dto.PagamentoMobileDtos.*;
import com.mais.seguradora.enummerated.*;
import com.mais.seguradora.exception.PagamentoMobileException;
import com.mais.seguradora.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Pagamento acadêmico de um registro já existente. Não cria cobrança nem chama gateway. */
@Service
@Transactional(readOnly = true)
public class PagamentoMobileService {
    private static final String FICTICIO = "FICTICIO";
    private final PagamentoRepository pagamentos;
    private final ContratacaoRepository contratacoes;
    private final ClienteRepository clientes;
    private final CorretorRepository corretores;
    private final AdminRepository admins;

    public PagamentoMobileService(PagamentoRepository pagamentos, ContratacaoRepository contratacoes,
            ClienteRepository clientes, CorretorRepository corretores, AdminRepository admins) {
        this.pagamentos = pagamentos; this.contratacoes = contratacoes; this.clientes = clientes;
        this.corretores = corretores; this.admins = admins;
    }
    public List<Resumo> cliente(Integer id) {
        confirmar(id, () -> clientes.existsById(id));
        return ordenar(pagamentos.findByContratacao_Cliente_IdOrderByIdDesc(id)).stream()
                .filter(p -> pertence(p, id)).map(p -> resumo(p, true, false)).toList();
    }
    public Resumo detalhe(Integer clienteId, Integer id) {
        confirmar(clienteId, () -> clientes.existsById(clienteId));
        return resumo(localizar(clienteId, id), true, false);
    }
    public List<Resumo> corretor(Integer id) {
        confirmar(id, () -> corretores.existsById(id));
        return ordenar(pagamentos.findDistinctByContratacao_Apolice_Corretores_IdOrderByIdDesc(id)).stream()
                .filter(p -> p.getContratacao().getApolice() != null && p.getContratacao().getApolice().getCorretores() != null
                        && p.getContratacao().getApolice().getCorretores().stream().anyMatch(c -> c != null && Objects.equals(c.getId(), id)))
                .map(p -> resumo(p, false, true)).toList();
    }
    public List<Resumo> admin(Integer id) {
        confirmar(id, () -> admins.existsById(id));
        return ordenar(pagamentos.findAll()).stream().map(p -> resumo(p, false, true)).toList();
    }

    @Transactional
    public Resumo pagar(Integer clienteId, Integer id, Confirmacao confirmado) {
        confirmar(clienteId, () -> clientes.existsById(clienteId));
        if (id == null || id <= 0) throw ausente();
        Integer contratoId = pagamentos.findContratacaoForCliente(id, clienteId).orElseThrow(PagamentoMobileService::ausente);
        // Ordem única: contratação primeiro, depois todos os seus pagamentos por ID.
        Contratacao contrato = contratacoes.findForPagamento(contratoId, clienteId).orElseThrow(PagamentoMobileService::ausente);
        if (contrato.getCliente() == null || !Objects.equals(contrato.getCliente().getId(), clienteId)) throw ausente();
        List<Pagamento> registros = pagamentos.findForMobileOperation(contratoId);
        Pagamento pagamento = registros.stream().filter(p -> Objects.equals(p.getId(), id) && pertence(p, clienteId)
                && Objects.equals(p.getContratacao().getId(), contratoId)).findFirst().orElseThrow(PagamentoMobileService::ausente);
        if (!valorValido(confirmado.valor()) || pagamento.getValor() == null || pagamento.getValor().compareTo(confirmado.valor()) != 0)
            throw conflito("O valor mudou. Atualize o pagamento antes de confirmar.");
        // Repetir o mesmo pagamento não modifica data, valor, método nem contrato.
        if (pagamento.getStatus() == StatusPagamento.APROVADA && FICTICIO.equals(pagamento.getFormaPagamento())
                && pagamento.getDataPagamento() != null)
            return resumo(pagamento, true, false, registros);
        String impedimento = impedimento(pagamento, contrato, registros);
        if (impedimento != null) throw conflito(impedimento);
        pagamento.setStatus(StatusPagamento.APROVADA);
        pagamento.setFormaPagamento(FICTICIO);
        pagamento.setDataPagamento(LocalDate.now());
        pagamento.setIdApolice(contrato.getApolice().getId());
        pagamentos.save(pagamento);
        return resumo(pagamento, true, false, registros);
    }

    private Pagamento localizar(Integer clienteId, Integer id) {
        if (id == null || id <= 0) throw ausente();
        Pagamento p = pagamentos.findByIdAndContratacao_Cliente_Id(id, clienteId).orElseThrow(PagamentoMobileService::ausente);
        if (!integro(p) || !pertence(p, clienteId)) throw ausente();
        return p;
    }
    private Resumo resumo(Pagamento p, boolean cliente, boolean mostrarNome) {
        List<Pagamento> registros = cliente ? pagamentos.findByContratacao_IdOrderByIdAsc(p.getContratacao().getId()) : List.of();
        return resumo(p, cliente, mostrarNome, registros);
    }
    private Resumo resumo(Pagamento p, boolean cliente, boolean mostrarNome, List<Pagamento> registros) {
        Contratacao c = p.getContratacao();
        String motivo = cliente ? impedimento(p, c, registros) : "Consulta disponível para este perfil.";
        String tipo = c.getApolice() == null ? null : c.getApolice().getTipoApolice();
        return new Resumo(p.getId(), c.getId(), tipo == null || tipo.isBlank() ? "Seguro não informado" : tipo,
                c.getApolice() == null ? null : c.getApolice().getNumeroApolice(),
                p.getStatus() == null ? "NAO_INFORMADO" : p.getStatus().name(), c.getStatus() == null ? "NAO_INFORMADO" : c.getStatus().name(),
                p.getValor(), p.getDataPagamento(), p.getDataVencimento(), p.getFormaPagamento(), FICTICIO.equals(p.getFormaPagamento()),
                cliente && motivo == null, motivo, mostrarNome ? c.getCliente().getNome() : null);
    }
    private static String impedimento(Pagamento p, Contratacao c, List<Pagamento> registros) {
        if (p.getStatus() == StatusPagamento.APROVADA) return "Pagamento já registrado no sistema.";
        if (p.getStatus() != StatusPagamento.PENDENTE) return "Este pagamento não está pendente.";
        if (c.getStatus() != StatusContratacao.APOLICE_EMITIDA) return "Pagamento disponível somente para apólice emitida.";
        if (registros.size() != 1 || !Objects.equals(registros.getFirst().getId(), p.getId()))
            return "Há mais de um registro para esta contratação. Solicite a conferência dos dados.";
        if (!valorValido(p.getValor()) || !valorValido(c.getValorContratacao()) || p.getValor().compareTo(c.getValorContratacao()) != 0)
            return "O valor do pagamento precisa ser conferido no sistema.";
        if (c.getApolice() == null || c.getApolice().getId() == null || c.getApolice().getId() <= 0 || p.getDataPagamento() != null
                || (p.getIdApolice() != null && !Objects.equals(p.getIdApolice(), c.getApolice().getId())))
            return "Os dados do pagamento precisam ser conferidos no sistema.";
        return null;
    }
    private static List<Pagamento> ordenar(List<Pagamento> lista) {
        return lista.stream().filter(PagamentoMobileService::integro)
                .sorted(Comparator.comparing(Pagamento::getId, Comparator.reverseOrder())).toList();
    }
    private static boolean integro(Pagamento p) {
        return p != null && p.getId() != null && p.getId() > 0 && p.getContratacao() != null && p.getContratacao().getId() != null
                && p.getContratacao().getId() > 0 && p.getContratacao().getCliente() != null
                && p.getContratacao().getCliente().getId() != null && p.getContratacao().getCliente().getId() > 0;
    }
    private static boolean pertence(Pagamento p, Integer id) {
        return integro(p) && Objects.equals(p.getContratacao().getCliente().getId(), id);
    }
    private static boolean valorValido(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ONE) >= 0 && valor.compareTo(new BigDecimal("99999999.99")) <= 0
                && Math.max(0, valor.stripTrailingZeros().scale()) <= 2;
    }
    private static void confirmar(Integer id, java.util.function.Supplier<Boolean> existe) {
        if (id == null || id <= 0 || !existe.get()) throw new PagamentoMobileException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
    }
    private static PagamentoMobileException ausente() { return new PagamentoMobileException(404, "NAO_ENCONTRADO", "Pagamento não encontrado."); }
    private static PagamentoMobileException conflito(String message) { return new PagamentoMobileException(409, "PAGAMENTO_NAO_CONFIRMADO", message); }
}
