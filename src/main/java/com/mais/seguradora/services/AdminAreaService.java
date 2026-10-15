package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.dto.AdminAreaDtos.*;
import com.mais.seguradora.dto.SolicitacaoResumoMapper;
import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.exception.AdminAreaException;
import com.mais.seguradora.model.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminAreaService {
    private final AdminRepository admins;
    private final ClienteRepository clientes;
    private final CorretorRepository corretores;
    private final ApoliceRepository produtos;
    private final ContratacaoRepository solicitacoes;
    private final CorretorService cadastro;
    private final ReceitaDashboardService receitas;
    public AdminAreaService(AdminRepository admins, ClienteRepository clientes, CorretorRepository corretores,
            ApoliceRepository produtos, ContratacaoRepository solicitacoes, CorretorService cadastro, ReceitaDashboardService receitas) {
        this.admins = admins; this.clientes = clientes; this.corretores = corretores; this.produtos = produtos;
        this.solicitacoes = solicitacoes; this.cadastro = cadastro; this.receitas = receitas;
    }
    public Dashboard dashboard(Integer adminId) {
        confirmarAdmin(adminId);
        var lista = enviadas();
        return new Dashboard(clientes.count(), corretores.count(), lista.stream().filter(c ->
                Set.of(StatusContratacao.PENDENTE, StatusContratacao.ANALISANDO, StatusContratacao.APROVADA).contains(c.getStatus())).count(),
                lista.stream().filter(c -> c.getStatus() == StatusContratacao.APOLICE_EMITIDA).count(), receitas.resumo());
    }
    public List<ClienteResumo> clientes(Integer adminId) {
        confirmarAdmin(adminId);
        return clientes.findAll().stream().sorted(Comparator.comparing(Cliente::getId)).map(c ->
                new ClienteResumo(c.getId(), c.getNome(), c.getEmail(), c.getTelefone())).toList();
    }
    public List<CorretorResumo> corretores(Integer adminId) {
        confirmarAdmin(adminId);
        return corretores.findAll().stream().sorted(Comparator.comparing(Corretor::getId)).map(this::resumo).toList();
    }
    public List<Produto> produtos(Integer adminId) {
        confirmarAdmin(adminId);
        return produtos.findAll().stream().sorted(Comparator.comparing(Apolice::getId)).map(AdminAreaService::produto).toList();
    }
    public List<Solicitacao> solicitacoes(Integer adminId, boolean emitidas) {
        confirmarAdmin(adminId);
        return enviadas().stream().filter(c -> !emitidas || c.getStatus() == StatusContratacao.APOLICE_EMITIDA).map(this::resumo).toList();
    }
    public Solicitacao detalhe(Integer adminId, Integer id) {
        confirmarAdmin(adminId);
        if (id == null || id <= 0) throw naoEncontrado();
        var c = solicitacoes.findById(id).orElseThrow(AdminAreaService::naoEncontrado);
        if (!enviada(c)) throw naoEncontrado();
        return resumo(c);
    }
    @Transactional
    public CorretorResumo cadastrar(Integer adminId, NovoCorretor pedido) {
        confirmarAdmin(adminId);
        var catalogo = produtos.findAllForAdminOperation();
        if (catalogo.stream().noneMatch(a -> Objects.equals(a.getId(), pedido.produtoId()))) throw naoEncontrado();
        String email = pedido.email().trim().toLowerCase(Locale.ROOT);
        if (corretores.existsByEmailIgnoreCase(email) || clientes.existsByEmailIgnoreCase(email) || admins.existsByEmailIgnoreCase(email))
            throw conflito("E-mail já utilizado. Confira a lista antes de cadastrar.");
        if (corretores.findAll().stream().anyMatch(c -> numeros(c.getCpf()).equals(numeros(pedido.cpf()))))
            throw conflito("CPF já utilizado por um corretor. Confira a lista antes de cadastrar.");
        if (pedido.senha().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new AdminAreaException(400, "DADOS_INVALIDOS", "A senha deve ocupar no máximo 72 bytes.");
        Corretor c = new Corretor();
        c.setNome(pedido.nome().trim()); c.setCpf(numeros(pedido.cpf())); c.setEmail(email);
        c.setTelefone(pedido.telefone().trim()); c.setSenha(pedido.senha());
        // Reaproveita o cadastro existente: BCrypt e vínculo inicial com o produto.
        return resumo(cadastro.cadastrar(c, pedido.produtoId()));
    }
    @Transactional
    public CorretorResumo vincular(Integer adminId, Integer corretorId, Vinculos pedido) {
        confirmarAdmin(adminId);
        var catalogo = produtos.findAllForAdminOperation();
        if (corretorId == null || corretorId <= 0) throw naoEncontrado();
        var corretor = corretores.findForAdminOperation(corretorId).orElseThrow(AdminAreaService::naoEncontrado);
        var ids = new HashSet<>(pedido.produtoIds());
        if (ids.size() != pedido.produtoIds().size()) throw new AdminAreaException(400, "DADOS_INVALIDOS", "Há produtos repetidos.");
        if (!catalogo.stream().map(Apolice::getId).collect(java.util.stream.Collectors.toSet()).containsAll(ids)) throw naoEncontrado();
        // Apolice é o lado proprietário. Não altera nem remove contratações, pagamentos ou históricos.
        for (Apolice a : catalogo) {
            boolean atual = a.getCorretores().stream().anyMatch(c -> Objects.equals(c.getId(), corretorId));
            boolean novo = ids.contains(a.getId());
            if (atual && !novo) a.getCorretores().removeIf(c -> Objects.equals(c.getId(), corretorId));
            else if (!atual && novo) a.getCorretores().add(corretor);
            if (atual != novo) produtos.save(a);
        }
        corretor.setApolices(new ArrayList<>(catalogo.stream().filter(a -> ids.contains(a.getId())).toList()));
        return resumo(corretor);
    }
    private void confirmarAdmin(Integer id) {
        if (id == null || id <= 0 || !admins.existsById(id))
            throw new AdminAreaException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
    }
    private List<Contratacao> enviadas() {
        return solicitacoes.findAll().stream().filter(AdminAreaService::enviada)
                .sorted(Comparator.comparing(Contratacao::getId, Comparator.reverseOrder())).toList();
    }
    private static boolean enviada(Contratacao c) {
        return c != null && c.getId() != null && c.getCliente() != null && c.getCliente().getId() != null
                && c.getStatus() != null && c.getStatus() != StatusContratacao.AGUARDANDO_BEM;
    }
    private CorretorResumo resumo(Corretor c) {
        return new CorretorResumo(c.getId(), c.getNome(), c.getEmail(), c.getTelefone(), c.getApolices().stream()
                .sorted(Comparator.comparing(Apolice::getId)).map(AdminAreaService::produto).toList());
    }
    private Solicitacao resumo(Contratacao c) { return new Solicitacao(SolicitacaoResumoMapper.resumo(c), c.getCliente().getNome()); }
    private static Produto produto(Apolice a) { return new Produto(a.getId(), a.getTipoApolice(), a.getNumeroApolice()); }
    private static String numeros(String valor) { return valor == null ? "" : valor.replaceAll("[^0-9]", ""); }
    private static AdminAreaException naoEncontrado() { return new AdminAreaException(404, "NAO_ENCONTRADO", "Registro não encontrado."); }
    private static AdminAreaException conflito(String mensagem) { return new AdminAreaException(409, "CADASTRO_EXISTENTE", mensagem); }
}
