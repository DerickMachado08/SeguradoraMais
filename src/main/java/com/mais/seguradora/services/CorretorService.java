package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ApoliceRepository;
import com.mais.seguradora.Repositories.CorretorRepository;
import com.mais.seguradora.exception.RecursoNullException;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Corretor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorretorService {

    @Autowired
    private ApoliceRepository apoliceRepository;

    @Autowired
    private CorretorRepository corretorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public Corretor cadastrar(Corretor corretor, Integer idApolice) {

        Apolice apolice = apoliceRepository.findById(idApolice)
                .orElseThrow(() -> new RuntimeException("Apólice não encontrada"));

        corretor.getApolices().add(apolice);
        apolice.getCorretores().add(corretor);

        corretor.setSenha(passwordEncoder.encode(corretor.getSenha()));

        corretorRepository.save(corretor);
        apoliceRepository.save(apolice);


        return corretor;
    }


    public Corretor autenticar(String email, String senha) {
        Corretor corretor = corretorRepository.findByEmail(email).orElse(null);

        if (corretor != null &&
                passwordEncoder.matches(senha, corretor.getSenha())) {

            return corretor;
        }
        return null;
    }


    public List<Corretor> listarTodos() {
        return corretorRepository.findAll();
    }

    public long totalCorretores() {
        return corretorRepository.count();
    }

    public void deletarCorretor(Integer id) {

        Corretor corretor = corretorRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNullException(
                                "Corretor não encontrado"
                        )
                );

        for(Apolice apolice: corretor.getApolices()){
            apolice.getCorretores().remove(corretor);
            apoliceRepository.save(apolice);
        }

        corretorRepository.delete(corretor);
    }
}
