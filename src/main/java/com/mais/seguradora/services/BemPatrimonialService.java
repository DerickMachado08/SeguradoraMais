package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.BemPatrimonialRepository;
import com.mais.seguradora.exception.BuscarPorId;
import com.mais.seguradora.model.BemPatrimonial;
import com.mais.seguradora.model.Contratacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BemPatrimonialService {
    @Autowired
    private BemPatrimonialRepository bemPatrimonialRepository;

    public BemPatrimonial buscarPorContratacao(Integer id){
        return bemPatrimonialRepository.findByContratacaoId(id).orElseThrow(() -> new BuscarPorId());

    }
}
