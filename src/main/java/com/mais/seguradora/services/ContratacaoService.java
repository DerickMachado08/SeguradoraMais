package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ContratacaoRepository;
import com.mais.seguradora.Repositories.PagamentoRepository;
import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.enummerated.StatusPagamento;
import com.mais.seguradora.exception.ContratoNullException;
import com.mais.seguradora.exception.ContratoPendenteNullException;
import com.mais.seguradora.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContratacaoService {

    @Autowired
    private ContratacaoRepository contratacaoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    public List<Contratacao> buscarPorCliente(Cliente cliente) {
        return contratacaoRepository.findByCliente(cliente);
    }

    public void salvar(Contratacao contratacao) {
        contratacaoRepository.save(contratacao);
    }

    public Contratacao emitirApolice(Integer idContratacao, BigDecimal valor, Integer parcelas) {
        Contratacao contratacao = contratacaoRepository.findById(idContratacao)
                .orElseThrow(() -> new ContratoNullException());

        if (contratacao.getStatus() != StatusContratacao.PENDENTE) {
            throw new ContratoPendenteNullException();
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

    public Contratacao buscarPorId(Integer id){
        return contratacaoRepository.findById(id).orElseThrow(() -> new RuntimeException("id não encontrado"));
    }

    public boolean jaPossui(Cliente cliente, Apolice apolice){
        return contratacaoRepository.existsByClienteAndApolice(cliente, apolice);
    }


    public List<Contratacao> contratacoesPendentes(Corretor corretor) {
        return contratacaoRepository.findByStatusAndApoliceCorretor(
                StatusContratacao.PENDENTE,
                corretor
        );
    }
}
