package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ApoliceRepository;
import com.mais.seguradora.model.Apolice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;

@Service
public class ApoliceService {

    @Autowired
    private ApoliceRepository apoliceRepository;


    public Apolice criarApolice (Apolice apolice){

        Apolice salva = apoliceRepository.save(apolice);

        String numeroApolice = "AP-"+ LocalDate.now().getYear() + "-" + salva.getId();

        salva.setNumeroApolice(numeroApolice);

        return apoliceRepository.save(salva);
    }

    public Long totalApolice(){
        return apoliceRepository.count();
    }

    public List<Apolice> ListarApolice(){
        return apoliceRepository.findAll();
    }

    public Apolice buscarPorId(Integer id){
        return apoliceRepository.findById(id).orElseThrow(() -> new RuntimeException("id não encontrado"));
    }




}
