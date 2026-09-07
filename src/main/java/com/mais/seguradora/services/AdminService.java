package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.AdminRepository;
import com.mais.seguradora.Repositories.SinistroRepository;
import com.mais.seguradora.enummerated.StatusSinistro;
import com.mais.seguradora.exception.RecursoNullException;
import com.mais.seguradora.model.Admin;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Sinistro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SinistroRepository sinistroRepository;

    @Autowired
    private EmailService emailService;

    public Admin autenticar(String email, String senha){
        return adminRepository.findByEmailAndSenha(email, senha).orElse(null);
    }

    public void aprovarSinistro(Integer id){

        Sinistro sinistro = sinistroRepository.findById(id)
                .orElseThrow(()-> new RecursoNullException("Sinistro não encontrado"));

        sinistro.setStatus(StatusSinistro.APROVADO);

        sinistroRepository.save(sinistro);

        String email = sinistro.getCliente().getEmail();

        emailService.sinistroAprovada(email);
    }

    public void negarSinistro(Integer id){

        Sinistro sinistro = sinistroRepository.findById(id)
                .orElseThrow(()-> new RecursoNullException("Sinistro não encontrado"));

        sinistro.setStatus(StatusSinistro.NEGADO);

        sinistroRepository.save(sinistro);

        String email = sinistro.getCliente().getEmail();

        emailService.sinistroNegado(email);
    }

}
