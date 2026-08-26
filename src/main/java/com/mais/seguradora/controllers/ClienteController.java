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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Cliente cliente, HttpSession session) {

        Cliente autenticado = service.autenticar(
                cliente.getEmail(),
                cliente.getSenha()
        );

        if (autenticado != null) {
            session.setAttribute("usuarioLogado", autenticado);
            return ResponseEntity.ok(Map.of("status", "ok"));
        }

        System.out.println("Login inválido");
        return ResponseEntity.badRequest()
                .body(Map.of("status", "erro"));
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        System.out.println("Entrou no dashboard");

        Cliente cliente = (Cliente) session.getAttribute("clienteLogado");

        System.out.println("Cliente da sessão: " + cliente);

        if(cliente == null) {
            return "redirect:/login";
        }

        model.addAttribute("cliente", cliente);

        return "dashboardCliente";
    }

    @GetMapping("/confirmar-exclusao")
    public String abrirConfirmacaoExclusao() {
        return "confirmar-exclusao";
    }

    @GetMapping ("/{id}")
    public Cliente buscar(@PathVariable("id") Integer id){
        return service.buscar(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable("id") Integer id){
        service.deletar(id);
    }

    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable("id") Integer id,@Valid @RequestBody Cliente cliente){
        return service.atualizar(id, cliente);
    }


}
