package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ContratacaoRepository;
import com.mais.seguradora.exception.BuscarPorId;
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

    public String criarCheckout(Integer idContratacao) throws StripeException {
        Contratacao contracao = contratacaoRepository.findById(idContratacao).orElseThrow(() -> new BuscarPorId());

        String nomeProduto = contracao.getApolice().getTipoApolice();

        BigDecimal valor = contracao.getValorContratacao();

        String email = contracao.getCliente().getEmail();
        long valorCentavos =
                valor.multiply(BigDecimal.valueOf(100)).longValue();

        SessionCreateParams params =
                SessionCreateParams.builder()

                        .setMode(
                                SessionCreateParams.Mode.PAYMENT
                        )

                        .setSuccessUrl(
                                "http://localhost:8080/pagamento/sucesso"
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