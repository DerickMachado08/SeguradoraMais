package com.mais.seguradora.controllers;

import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    private static final String admin = "admin@gmail.com";

    @Autowired
    private ClienteService service;

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody Cliente cliente) {
        try {
            Cliente clienteSalvo = service.cadastrar(cliente);
            return ResponseEntity.ok(clienteSalvo);
        } catch (RuntimeException e) {
            // Isso envia um JSON com a mensagem para o seu JavaScript capturar no .catch()
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Cliente cliente) {

        Cliente c = service.autenticar(cliente.getEmail(), cliente.getSenha());

        Map<String, String> resposta = new HashMap<>();

        if (c != null) {
            resposta.put("status", "ok");
        } else {
            resposta.put("status", "erro");
        }

        return resposta;
    }

    @GetMapping
    public List<Cliente> listar(){
        return service.listar();
    }

    @GetMapping ("/{id}")
    public Cliente buscar(@PathVariable Integer id){
        return service.buscar(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Integer id){
        service.deletar(id);
    }

    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable Integer id,@Valid @RequestBody Cliente cliente){
        return service.atualizar(id, cliente);
    }


}
