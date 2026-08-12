package com.mais.seguradora.controllers;

import com.mais.seguradora.services.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("pagamento")
public class PagamentoController {

    @Autowired
    private StripeService stripeService;

    @PostMapping("/checkout/{idContratacao}")
    public ResponseEntity<?> criarCheclkout(@PathVariable("idContratacao") Integer idContratacao){
        try {
           String url = stripeService.criarCheckout(idContratacao);
           return ResponseEntity.ok(Map.of("url", url));
        } catch (StripeException e){
            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "erro",
                                    e.getMessage()));
        }
    }

    @GetMapping("/sucesso")
    public String sucesso() {
        return "pagamento-sucesso";
    }

    @GetMapping("/cancelado")
    public String cancelado() {
        return "pagamento-cancelado";
    }
}
