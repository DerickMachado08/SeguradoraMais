package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.CorretorRepository;
import com.mais.seguradora.model.Corretor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CorretorService {
    @Autowired
    private CorretorRepository corretorRepository;

    public Corretor autenticar(String email, String senha){
        return corretorRepository.findByEmailAndSenha(email,senha).orElse(null);
    }
}
