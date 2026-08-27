package com.mais.seguradora.controllers;

import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.services.ClienteService;
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
    public ResponseEntity<?> cadastrar(@Valid @RequestBody Cliente cliente) {
            Cliente clienteSalvo = service.cadastrar(cliente);
            return ResponseEntity.ok(clienteSalvo);
    }

    @GetMapping("/confirmar-exclusao")
    public String abrirConfirmacaoExclusao() {
        return "confirmar-exclusao";
    }

    @GetMapping ("/{id}")
    public Cliente buscar(@PathVariable("id") Integer id){
        return service.buscar(id);
    }

    @PostMapping("/deletar-conta")
    public String deletarConta(@RequestParam String email, @RequestParam String senha, HttpSession session, RedirectAttributes redirectAttributes){
    Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");

        if (cliente == null){
        return "redirect:/login";
    }
        try {
            service.deletarConta(cliente.getId(),email,senha);

            session.invalidate();

            return "redirect:/login";
        } catch (RuntimeException e){
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "confirmar-exclusao";
    }

    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable("id") Integer id,@Valid @RequestBody Cliente cliente){
        return service.atualizar(id, cliente);
    }


}
