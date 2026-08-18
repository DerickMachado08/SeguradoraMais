package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.BemPatrimonialRepository;
import com.mais.seguradora.exception.RecursoNullException;
import com.mais.seguradora.model.BemPatrimonial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BemPatrimonialService {
    @Autowired
    private BemPatrimonialRepository bemPatrimonialRepository;

    public Optional<BemPatrimonial> buscarOpcionalPorContratacao(Integer id) {
        return bemPatrimonialRepository.findByContratacaoId(id);
    }

    public BemPatrimonial buscarPorContratacao(Integer id) {
        return buscarOpcionalPorContratacao(id).orElseThrow(() -> new RecursoNullException("id não encontrado"));
    }
}