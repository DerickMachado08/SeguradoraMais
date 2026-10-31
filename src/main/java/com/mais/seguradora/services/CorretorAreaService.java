package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.dto.CorretorAreaDtos.*;
import com.mais.seguradora.dto.SolicitacaoResumoMapper;
import com.mais.seguradora.enummerated.*;
import com.mais.seguradora.exception.CorretorAreaException;
import com.mais.seguradora.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Escopo aprovado por produto; não representa atribuição individual por contrato. */
@Service
@Transactional(readOnly = true)
public class CorretorAreaService {
    private static final Set<StatusContratacao> EM_ANDAMENTO = EnumSet.of(
            StatusContratacao.PENDENTE, StatusContratacao.ANALISANDO, StatusContratacao.APROVADA);
    private final CorretorRepository corretores;
    private final ContratacaoRepository contratacoes;
    private final BemPatrimonialRepository bens;
    private final PagamentoRepository pagamentos;
    public CorretorAreaService(CorretorRepository corretores, ContratacaoRepository contratacoes,
            BemPatrimonialRepository bens, PagamentoRepository pagamentos) {
        this.corretores = corretores; this.contratacoes = contratacoes; this.bens = bens; this.pagamentos = pagamentos;
    }
    public Dashboard dashboard(Integer corretorId) {
        List<Contratacao> lista = vinculadas(corretorId);
        return new Dashboard(lista.size(), lista.stream().filter(c -> EM_ANDAMENTO.contains(c.getStatus())).count(),
                lista.stream().filter(c -> c.getStatus() == StatusContratacao.APOLICE_EMITIDA).count(),
                lista.stream().limit(5).map(this::resumo).toList());
    }
    public List<Solicitacao> solicitacoes(Integer corretorId) { return vinculadas(corretorId).stream().map(this::resumo).toList(); }
    public List<Solicitacao> apolices(Integer corretorId) {
        return vinculadas(corretorId).stream().filter(c -> c.getStatus() == StatusContratacao.APOLICE_EMITIDA).map(this::resumo).toList();
    }
    public Solicitacao detalhe(Integer corretorId, Integer id) { return resumo(localizar(corretorId, id, false)); }

    @Transactional
    public Solicitacao definirPreco(Integer corretorId, Integer id, Preco pedido) {
        Contratacao c = localizar(corretorId, id, true);
        pendente(c); validarPreco(pedido.valor());
        c.setValorContratacao(pedido.valor()); contratacoes.save(c);
        return resumo(c);
    }
    /** O preço confirmado pelo usuário deve continuar sendo o preço salvo sob o lock. */
    @Transactional
    public Solicitacao emitir(Integer corretorId, Integer id, Preco confirmado) {
        Contratacao c = localizar(corretorId, id, true);
        pendente(c); validarPreco(confirmado.valor()); validarPreco(c.getValorContratacao());
        if (c.getValorContratacao().compareTo(confirmado.valor()) != 0)
            throw conflito("O preço foi alterado. Atualize os dados antes de confirmar a emissão.");
        BemPatrimonial bem = bens.findByContratacaoId(id).orElseThrow(() -> conflito("A solicitação precisa de um bem cadastrado."));
        if (bem.getCliente() == null || !Objects.equals(bem.getCliente().getId(), c.getCliente().getId())
                || bem.getContratacao() == null || !Objects.equals(bem.getContratacao().getId(), c.getId()))
            throw conflito("Não foi possível confirmar o bem da solicitação.");
        if (pagamentos.existsByContratacao_Id(id)) throw conflito("Já existe um pagamento vinculado. Atualize os dados.");
        c.setStatus(StatusContratacao.APOLICE_EMITIDA);
        contratacoes.save(c);
        Pagamento pagamento = new Pagamento();
        pagamento.setContratacao(c); pagamento.setValor(c.getValorContratacao());
        pagamento.setFormaPagamento("PENDENTE"); pagamento.setStatus(StatusPagamento.PENDENTE);
        // Prazo de pagamento existente; não é vencimento/expiração da apólice.
        pagamento.setDataVencimento(LocalDate.now().plusDays(7));
        pagamentos.save(pagamento);
        return resumo(c);
    }
    @Transactional
    public Solicitacao recusar(Integer corretorId, Integer id) {
        Contratacao c = localizar(corretorId, id, true);
        if (c.getStatus() == StatusContratacao.REJEITADA) return resumo(c);
        if (!EM_ANDAMENTO.contains(c.getStatus())) throw conflito("Esta solicitação não pode mais ser recusada.");
        c.setStatus(StatusContratacao.REJEITADA); contratacoes.save(c);
        return resumo(c);
    }
    private List<Contratacao> vinculadas(Integer corretorId) {
        confirmarCorretor(corretorId);
        return contratacoes.findDistinctByApolice_Corretores_Id(corretorId).stream()
                .filter(c -> pertence(c, corretorId) && enviada(c))
                .sorted(Comparator.comparing(Contratacao::getId, Comparator.reverseOrder())).toList();
    }
    private Contratacao localizar(Integer corretorId, Integer id, boolean lock) {
        confirmarCorretor(corretorId);
        if (id == null || id <= 0) throw naoEncontrada();
        Contratacao c = (lock ? contratacoes.findForCorretorOperation(id, corretorId)
                : contratacoes.findByIdAndApolice_Corretores_Id(id, corretorId)).orElseThrow(CorretorAreaService::naoEncontrada);
        if (!pertence(c, corretorId) || !enviada(c)) throw naoEncontrada();
        return c;
    }
    private void confirmarCorretor(Integer id) {
        if (id == null || id <= 0 || !corretores.existsById(id))
            throw new CorretorAreaException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
    }
    private static boolean pertence(Contratacao c, Integer id) {
        return c != null && c.getApolice() != null && c.getApolice().getCorretores() != null
                && c.getApolice().getCorretores().stream().anyMatch(r -> r != null && Objects.equals(r.getId(), id));
    }
    private static boolean enviada(Contratacao c) {
        return c.getId() != null && c.getCliente() != null && c.getCliente().getId() != null
                && c.getStatus() != null && c.getStatus() != StatusContratacao.AGUARDANDO_BEM;
    }
    private Solicitacao resumo(Contratacao c) {
        boolean pendente = c.getStatus() == StatusContratacao.PENDENTE;
        return new Solicitacao(SolicitacaoResumoMapper.resumo(c), c.getCliente().getNome(), pendente,
                pendente && precoValido(c.getValorContratacao()), EM_ANDAMENTO.contains(c.getStatus()));
    }
    private static void pendente(Contratacao c) {
        if (c.getStatus() != StatusContratacao.PENDENTE) throw conflito("A solicitação mudou de estado. Atualize os dados.");
    }
    private static boolean precoValido(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ONE) >= 0 && valor.compareTo(new BigDecimal("99999999.99")) <= 0
                && Math.max(0, valor.stripTrailingZeros().scale()) <= 2;
    }
    private static void validarPreco(BigDecimal valor) { if (!precoValido(valor)) throw conflito("Defina um preço válido antes de emitir."); }
    private static CorretorAreaException naoEncontrada() { return new CorretorAreaException(404, "NAO_ENCONTRADA", "Solicitação não encontrada."); }
    private static CorretorAreaException conflito(String mensagem) { return new CorretorAreaException(409, "ESTADO_ALTERADO", mensagem); }
}
