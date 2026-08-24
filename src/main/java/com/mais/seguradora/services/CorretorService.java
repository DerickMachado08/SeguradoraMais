package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ApoliceRepository;
import com.mais.seguradora.Repositories.CorretorRepository;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Corretor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorretorService {

    @Autowired
    private ApoliceRepository apoliceRepository;

    @Autowired
    private CorretorRepository corretorRepository;


    public Corretor cadastrar(Corretor corretor, Integer idApolice) {

        Apolice apolice = apoliceRepository.findById(idApolice)
                .orElseThrow(() -> new RuntimeException("Apólice não encontrada"));

        corretor.getApolices().add(apolice);
        apolice.getCorretores().add(corretor);

        corretorRepository.save(corretor);
        apoliceRepository.save(apolice);

        return corretor;
    }


    public Corretor autenticar(String email, String senha) {
        return corretorRepository
                .findByEmailAndSenha(email, senha)
                .orElse(null);
    }


    public List<Corretor> listarTodos() {
        return corretorRepository.findAll();
    }
}