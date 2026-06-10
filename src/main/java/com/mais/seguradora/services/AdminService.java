package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.AdminRepository;
import com.mais.seguradora.model.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    public Admin autenticar(String email, String senha){
        return adminRepository.findByEmailAndSenha(email, senha).orElse(null);
    }
}
