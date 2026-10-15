package com.mais.seguradora.controllers;

import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.services.ClienteService;
import com.mais.seguradora.dto.CadastroClienteRequest;
import com.mais.seguradora.dto.ClienteResponse;
import org.springframework.http.CacheControl;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody CadastroClienteRequest cadastro) {
        Cliente clienteSalvo = service.cadastrar(cadastro.toEntity());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ClienteResponse.from(clienteSalvo));
    }

    @GetMapping("/confirmar-exclusao")
    public String abrirConfirmacaoExclusao() {
        return "outros/confirmar-exclusao";
    }

    @GetMapping ("/{id}")
    public Cliente buscar(@PathVariable("id") Integer id){
        return service.buscar(id);
    }

    @PostMapping("/deletar-conta")
    @ResponseBody
    public ResponseEntity<?> deletarConta(
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            HttpSession session) {

        Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");

        if (cliente == null) {
            return ResponseEntity.status(401).body("Usuário não autenticado");
        }

        service.deletarConta(
                cliente.getId(),
                email,
                senha
        );

        session.invalidate();

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable("id") Integer id,@Valid @RequestBody Cliente cliente){
        return service.atualizar(id, cliente);
    }


}
