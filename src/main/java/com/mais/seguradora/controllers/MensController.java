package com.mais.seguradora.controllers;

import com.mais.seguradora.Repositories.MensContatoRepsitory;
import com.mais.seguradora.model.MensagemContato;
import com.mais.seguradora.services.MensService;

import com.mais.seguradora.services.MensService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contato")
public class MensController {

    @Autowired
    private MensContatoRepsitory repository;

    @Autowired
    private MensService emailService;

    @PostMapping
    public ResponseEntity<String> contato(@Valid
            @RequestBody MensagemContato contato){

        emailService.salvar(contato);

        emailService.enviarContato(
                contato.getName(),
                contato.getEmail(),
                contato.getTelefone(),
                contato.getAssunto()
        );

        return ResponseEntity.ok(
                "Mensagem enviada com sucesso!"
        );
    }
}
