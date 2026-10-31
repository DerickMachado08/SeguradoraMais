package com.mais.seguradora.controllers;

import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.services.ApoliceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/apolice")
public class ApoliceController {

    @Autowired
    private ApoliceService apoliceService;

    @PostMapping()
    public ResponseEntity<?> criarApolice(@RequestBody Apolice apolice){
        Apolice apoliceSalvo = apoliceService.criarApolice(apolice);
        return (ResponseEntity.ok(apoliceSalvo));
    }


}
