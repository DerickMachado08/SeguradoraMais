package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ContratacaoRepository;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContratacaoService {

    @Autowired
    private ContratacaoRepository contratacaoRepository;

    public List<Contratacao> buscarPorCliente(Cliente cliente){
        return contratacaoRepository.findByCliente(cliente);
    }

    public void salvar(Contratacao contratacao){
        contratacaoRepository.save(contratacao);
    }
}
