package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.dto.ClienteAreaDtos.*;
import com.mais.seguradora.enummerated.StatusApolice;
import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.exception.ClienteAreaException;
import com.mais.seguradora.exception.RecursoDuplicadoException;
import com.mais.seguradora.model.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClienteAreaService {
    private static final Set<StatusContratacao> EM_ANDAMENTO = EnumSet.of(
            StatusContratacao.AGUARDANDO_BEM, StatusContratacao.PENDENTE,
            StatusContratacao.ANALISANDO, StatusContratacao.APROVADA);
    private final ClienteRepository clientes;
    private final ApoliceRepository apolices;
    private final ContratacaoRepository contratacoes;
    private final ContratacaoService fluxo;
    public ClienteAreaService(ClienteRepository clientes, ApoliceRepository apolices,
                              ContratacaoRepository contratacoes, ContratacaoService fluxo) {
        this.clientes = clientes; this.apolices = apolices; this.contratacoes = contratacoes; this.fluxo = fluxo;
    }

    public Dashboard dashboard(Integer clienteId) {
        List<Contratacao> lista = proprias(clienteId);
        return new Dashboard(lista.size(), lista.stream().filter(c -> c.getStatus() != null && EM_ANDAMENTO.contains(c.getStatus())).count(),
                lista.stream().filter(c -> c.getStatus() == StatusContratacao.APOLICE_EMITIDA).count(),
                lista.stream().limit(5).map(this::resumo).toList());
    }
    public List<Produto> catalogo(Integer clienteId) {
        List<Contratacao> lista = proprias(clienteId);
        return apolices.findAll().stream().filter(a -> a.getId() != null && a.getTipoApolice() != null && !a.getTipoApolice().isBlank())
                .sorted(Comparator.comparing(Apolice::getTipoApolice).thenComparing(Apolice::getId))
                .map(a -> {
                    boolean bloqueado = lista.stream().anyMatch(c -> c.getApolice() != null
                            && Objects.equals(c.getApolice().getTipoApolice(), a.getTipoApolice()) && bloqueia(c.getStatus()));
                    String motivo = a.getStatus() != StatusApolice.ATIVA ? "Este seguro não está disponível para novas solicitações."
                            : bloqueado ? "Você já possui uma solicitação ou apólice para este tipo de seguro." : null;
                    return produto(a, motivo == null, motivo);
                }).toList();
    }
    public List<Solicitacao> solicitacoes(Integer clienteId) { return proprias(clienteId).stream().map(this::resumo).toList(); }
    public List<Solicitacao> apolices(Integer clienteId) {
        return proprias(clienteId).stream().filter(c -> c.getStatus() == StatusContratacao.APOLICE_EMITIDA).map(this::resumo).toList();
    }
    public Solicitacao detalhe(Integer clienteId, Integer id) {
        if (id == null || id <= 0) throw naoEncontrada();
        Contratacao value = contratacoes.findByIdAndClienteId(id, clienteId).orElseThrow(ClienteAreaService::naoEncontrada);
        if (!pertence(value, clienteId)) throw naoEncontrada();
        return resumo(value);
    }

    /** Uma transação engloba rascunho e bem; o lock vale entre instâncias da API. */
    @Transactional
    public Solicitacao solicitar(Integer clienteId, NovaSolicitacao pedido) {
        Cliente cliente = clientes.findForSolicitacao(clienteId).orElseThrow(() ->
                new ClienteAreaException(401, "SESSAO_INVALIDA", "Entre novamente para continuar."));
        Apolice apolice = apolices.findById(pedido.apoliceId()).orElseThrow(() ->
                new ClienteAreaException(404, "SEGURO_INDISPONIVEL", "O seguro selecionado não está disponível."));
        if (apolice.getStatus() != StatusApolice.ATIVA || apolice.getTipoApolice() == null
                || apolice.getTipoApolice().isBlank() || apolice.getTipoApolice().length() > 50)
            throw new ClienteAreaException(409, "SEGURO_INDISPONIVEL", "O seguro selecionado não está disponível.");
        if (fluxo.jaPossui(cliente, apolice)) throw duplicada();
        final Contratacao contratacao;
        try { contratacao = fluxo.iniciarContratacao(cliente, apolice); }
        catch (RecursoDuplicadoException ex) { throw duplicada(); }
        if (!pertence(contratacao, clienteId) || contratacao.getStatus() != StatusContratacao.AGUARDANDO_BEM)
            throw duplicada();
        BemPatrimonial bem = new BemPatrimonial();
        bem.setTipoBem(apolice.getTipoApolice()); bem.setDescricao(pedido.descricao().trim());
        bem.setValorEstimado(pedido.valorEstimado()); bem.setDataAquisicao(pedido.dataAquisicao());
        fluxo.cadastrarBemDaContratacao(contratacao.getId(), cliente, bem);
        // Reutiliza o fluxo existente, sem envio de e-mail, preço, emissão ou pagamento.
        Solicitacao salvo = resumo(contratacao);
        return new Solicitacao(salvo.id(), salvo.dataSolicitacao(), salvo.status(), salvo.apolice(), salvo.valorSeguro(),
                List.of(new BemResumo(bem.getTipoBem(), bem.getDescricao(), bem.getValorEstimado(), bem.getDataAquisicao())));
    }
    private List<Contratacao> proprias(Integer clienteId) {
        return contratacoes.findByClienteId(clienteId).stream().filter(c -> pertence(c, clienteId))
                .sorted(Comparator.comparing(Contratacao::getId, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
    }
    private Solicitacao resumo(Contratacao c) {
        List<BemResumo> bens = c.getBensPatriomiais() == null ? List.of() : c.getBensPatriomiais().stream()
                .filter(b -> b.getCliente() != null && Objects.equals(b.getCliente().getId(), c.getCliente().getId()))
                .map(b -> new BemResumo(b.getTipoBem(), b.getDescricao(), b.getValorEstimado(), b.getDataAquisicao())).toList();
        return new Solicitacao(c.getId(), c.getDataContratacao(), c.getStatus() == null ? null : c.getStatus().name(),
                c.getApolice() == null ? null : produto(c.getApolice(), false, null), c.getValorContratacao(), bens);
    }
    private static Produto produto(Apolice a, boolean disponivel, String motivo) {
        List<CoberturaResumo> coberturas = a.getCoberturas() == null ? List.of() : a.getCoberturas().stream()
                .map(c -> new CoberturaResumo(c.getNome(), c.getDescricao(), c.getTipoCobertura())).toList();
        return new Produto(a.getId(), a.getTipoApolice(), a.getNumeroApolice(), a.getDescricao(), coberturas, disponivel, motivo);
    }
    private static boolean bloqueia(StatusContratacao status) {
        return status != StatusContratacao.REJEITADA && status != StatusContratacao.CANCELADA && status != StatusContratacao.AGUARDANDO_BEM;
    }
    private static boolean pertence(Contratacao c, Integer id) {
        return c != null && c.getCliente() != null && Objects.equals(c.getCliente().getId(), id);
    }
    private static ClienteAreaException naoEncontrada() { return new ClienteAreaException(404, "NAO_ENCONTRADA", "Solicitação não encontrada."); }
    private static ClienteAreaException duplicada() { return new ClienteAreaException(409, "SOLICITACAO_EXISTENTE", "Você já possui uma solicitação para este tipo de seguro. Consulte suas solicitações antes de reenviar."); }
}
