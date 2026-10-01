package com.mais.seguradora.controllers;

import com.mais.seguradora.model.MensagemContato;
import com.mais.seguradora.services.EmailService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/contato")
public class MensagemController {

    private static final Logger log = LoggerFactory.getLogger(MensagemController.class);

    @Autowired
    private EmailService emailService;

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> contato(@Valid
            @RequestBody MensagemContato contato){

        try {
            emailService.salvar(contato);
        } catch (DataAccessException e) {
            log.error("Falha ao salvar a mensagem de contato", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "codigo", "persistencia_indisponivel",
                    "mensagem", "Não foi possível registrar sua mensagem no momento. Tente novamente mais tarde."
            ));
        }

        try {
            emailService.enviarContato(
                    contato.getName(),
                    contato.getEmail(),
                    contato.getTelefone(),
                    contato.getAssunto()
            );
        } catch (MailException e) {
            log.error("Falha no envio SMTP da mensagem de contato", e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "codigo", "email_indisponivel",
                    "mensagem", "Sua mensagem foi registrada, mas o envio do e-mail está temporariamente indisponível."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "codigo", "contato_enviado",
                "mensagem", "Mensagem enviada com sucesso! Em breve entraremos em contato."
        ));
    }
}
