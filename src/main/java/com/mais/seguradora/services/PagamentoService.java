package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.PagamentoRepository;
import com.mais.seguradora.enummerated.StatusPagamento;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import com.mais.seguradora.model.Pagamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    public BigDecimal calcularTotalRecebido() {
        List<Pagamento> pagamentos = pagamentoRepository.findByStatus(StatusPagamento.APROVADA);

        return pagamentos.stream().map(Pagamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean pagamentoAprovado(Integer idContratacao) {

        return pagamentoRepository
                .findByContratacao_IdAndStatus(
                        idContratacao,
                        StatusPagamento.APROVADA
                )
                .isPresent();
    }
}
