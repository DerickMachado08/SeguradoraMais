package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ContratacaoRepository;
import com.mais.seguradora.Repositories.PagamentoRepository;
import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.enummerated.StatusPagamento;
import com.mais.seguradora.exception.RecursoNullException;
import com.mais.seguradora.model.Contratacao;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeService {

    @Autowired
    private ContratacaoRepository contratacaoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    public String criarCheckout(Integer idContratacao) throws StripeException {
        Contratacao contracao = contratacaoRepository.findById(idContratacao).orElseThrow(() -> new RecursoNullException("não encontrado"));

        if (contracao.getStatus() != StatusContratacao.APOLICE_EMITIDA) {
            throw new RuntimeException("Pagamento liberado apenas depois da emissão da apolice");
        }

        BigDecimal valor = contracao.getValorContratacao();

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Contratacao sem valor valido para pagamento");
        }

        pagamentoRepository
                .findByContratacao_IdAndStatus(
                        idContratacao,
                        StatusPagamento.APROVADA
                )
                .ifPresent(pagamento -> {
                    throw new RuntimeException(
                            "Este pagamento já foi aprovado."
                    );
                });


        pagamentoRepository.findByContratacao_IdAndStatus(idContratacao, StatusPagamento.PENDENTE).
                orElseThrow(()-> new RuntimeException("Não existe pagamento para esta contratação"));

        String nomeProduto = contracao.getApolice().getTipoApolice();
        String email = contracao.getCliente().getEmail();
        long valorCentavos = valor.multiply(BigDecimal.valueOf(100)).longValue();

        SessionCreateParams params =
                SessionCreateParams.builder()

                        .setMode(
                                SessionCreateParams.Mode.PAYMENT
                        )

                        .setSuccessUrl(
                                "http://localhost:8080/pagamento/sucesso/" + idContratacao
                        )

                        .setCancelUrl(
                                "http://localhost:8080/pagamento/cancelado"
                        )

                        .setCustomerEmail(email)

                        .addLineItem(
                                SessionCreateParams.LineItem.builder()

                                        .setQuantity(1L)

                                        .setPriceData(
                                                SessionCreateParams
                                                        .LineItem
                                                        .PriceData
                                                        .builder()

                                                        .setCurrency("brl")

                                                        .setUnitAmount(
                                                                valorCentavos
                                                        )

                                                        .setProductData(
                                                                SessionCreateParams
                                                                        .LineItem
                                                                        .PriceData
                                                                        .ProductData
                                                                        .builder()
                                                                        .setName(nomeProduto)
                                                                        .build()
                                                        ).build()
                                        ).build()
                        ).build();

        Session session = Session.create(params);


        return session.getUrl();
    }
}