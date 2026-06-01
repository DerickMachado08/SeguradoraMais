package com.mais.seguradora.controllers;

import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody Cliente cliente) {
            Cliente clienteSalvo = service.cadastrar(cliente);
            return ResponseEntity.ok(clienteSalvo);
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
