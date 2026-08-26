package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.BemPatrimonialRepository;
import com.mais.seguradora.Repositories.ContratacaoRepository;
import com.mais.seguradora.Repositories.PagamentoRepository;
import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.enummerated.StatusPagamento;
import com.mais.seguradora.exception.RecursoDuplicadoException;
import com.mais.seguradora.exception.RecursoNullException;
import com.mais.seguradora.model.*;
import jakarta.transaction.Transactional;
import org.hibernate.query.SelectionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ContratacaoService {

    @Autowired
    private ContratacaoRepository contratacaoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private BemPatrimonialRepository bemPatrimonialRepository;

    public List<Contratacao> buscarPorCliente(Cliente cliente) {
        return contratacaoRepository.findByCliente(cliente);
    }

    public void deletar(Integer id){
        Contratacao contratacao = contratacaoRepository.findById(id).orElseThrow(()-> new RecursoNullException("Contratação não encontrado"));

                contratacaoRepository.delete(contratacao);
    }

    public Contratacao buscarPorId(Integer id) {
        return contratacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNullException("Contratacao nao encontrada"));
    }

    public void salvar(Contratacao contratacao) {
        contratacaoRepository.save(contratacao);
    }

    public boolean jaPossui(Cliente cliente, Apolice apolice) {
        return contratacaoRepository
                .findByClienteAndApolice_TipoApoliceAndStatusNot(
                        cliente,
                        apolice.getTipoApolice(),
                        StatusContratacao.REJEITADA
                )
                .stream()
                .anyMatch(contratacao -> contratacao.getStatus() != StatusContratacao.AGUARDANDO_BEM);
    }

    public Optional<Contratacao> buscarRascunho(Cliente cliente, Apolice apolice) {
        return contratacaoRepository.findFirstByClienteAndApoliceAndStatus(
                cliente,
                apolice,
                StatusContratacao.AGUARDANDO_BEM
        );
    }

    @Transactional
    public Contratacao iniciarContratacao(Cliente cliente, Apolice apolice) {
        Optional<Contratacao> rascunhoExistente = buscarRascunho(cliente, apolice);

        if (rascunhoExistente.isPresent()) {
            return rascunhoExistente.get();
        }

        if (jaPossui(cliente, apolice)) {
            throw new RecursoDuplicadoException("Voce ja possui uma solicitacao para esse tipo de seguro");
        }

        Contratacao contratacao = new Contratacao();
        contratacao.setCliente(cliente);
        contratacao.setApolice(apolice);
        contratacao.setDataContratacao(LocalDate.now());
        contratacao.setStatus(StatusContratacao.AGUARDANDO_BEM);

        return contratacaoRepository.save(contratacao);
    }

    @Transactional
    public void cadastrarBemDaContratacao(Integer idContratacao, Cliente cliente, BemPatrimonial bem) {
        Contratacao contratacao = buscarPorId(idContratacao);

        if (!contratacao.getCliente().getId().equals(cliente.getId())) {
            throw new RuntimeException("Esta contratacao nao pertence ao cliente logado");
        }

        if (contratacao.getStatus() != StatusContratacao.AGUARDANDO_BEM) {
            throw new RuntimeException("Esta contratacao nao esta aguardando cadastro de bem");
        }

        if (bemPatrimonialRepository.findByContratacaoId(idContratacao).isPresent()) {
            throw new RuntimeException("Esta contratacao ja possui um bem cadastrado");
        }

        bem.setCliente(cliente);
        bem.setContratacao(contratacao);
        bemPatrimonialRepository.save(bem);

        contratacao.setStatus(StatusContratacao.PENDENTE);
        contratacaoRepository.save(contratacao);
    }

    @Transactional
    public Contratacao emitirApolice(Integer idContratacao, BigDecimal valor, Integer parcelas) {
        Contratacao contratacao = contratacaoRepository.findById(idContratacao)
                .orElseThrow(() -> new RecursoNullException("não foi possivel encontrar"));

        if (contratacao.getStatus() != StatusContratacao.PENDENTE) {
            throw new RecursoNullException("Contrato tem que esta pendente");
        }

        if (bemPatrimonialRepository.findByContratacaoId(idContratacao).isEmpty()) {
            throw new RuntimeException("Nao e possivel emitir apolice sem bem patrimonial");
        }

        contratacao.setStatus(StatusContratacao.APOLICE_EMITIDA);
        contratacao.setValorContratacao(valor);

        Contratacao contratacaoSalva = contratacaoRepository.save(contratacao);

        Pagamento pagamento = new Pagamento();
        pagamento.setContratacao(contratacaoSalva);
        pagamento.setValor(valor);
        pagamento.setQuantidadeParcela(parcelas);
        pagamento.setFormaPagamento("PENDENTE");
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setDataVencimento(LocalDate.now().plusDays(7));

        pagamentoRepository.save(pagamento);

        return contratacaoSalva;
    }

    public List<String> buscarTiposBloqueados(Cliente cliente) {

        return buscarPorCliente(cliente).stream()
                .filter(c -> c.getStatus() != StatusContratacao.REJEITADA)
                .map(c -> c.getApolice().getTipoApolice())
                .distinct()
                .toList();
    }

    public long totalApolicesEmitidas(List<Contratacao> contratacoes){
        return contratacoes.stream()
                .filter(contratacao -> contratacao.getStatus() != null)
                .filter(contratacao -> contratacao.getStatus() == StatusContratacao.APOLICE_EMITIDA)
                .count();
    }

    public List<Contratacao> contratacoesPendentes(Corretor corretor) {
        return contratacaoRepository.findByStatusAndApoliceCorretores(
                StatusContratacao.PENDENTE,
                corretor
        );
    }

    public long contarPendentes(List<Contratacao> contratacoes) {
        return contratacoes.stream()
                .filter(c -> c.getStatus() == StatusContratacao.PENDENTE)
                .count();
    }
}
