package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ApoliceRepository;
import com.mais.seguradora.Repositories.SinistroRepository;
import com.mais.seguradora.enummerated.StatusSinistro;
import com.mais.seguradora.exception.RecursoNullException;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Sinistro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class SinistroService {

    @Autowired
    private SinistroRepository sinistroRepository;

    @Autowired
    private ApoliceRepository apoliceRepository;

    @Autowired
    private EmailService emailService;


    public Sinistro abrir(Integer idApolice, String descricao, BigDecimal valorPrejuizo){
        Apolice apolice = apoliceRepository.findById(idApolice)
                .orElseThrow(()-> new RuntimeException("Apolice não encontrada"));

        Sinistro sinistro = new Sinistro();

        sinistro.setApolice(apolice);
        sinistro.setDescricao(descricao);
        sinistro.setValorPrejuizo(valorPrejuizo);
        sinistro.setDataSinsitro(LocalDate.now());

        sinistro.setStatus(StatusSinistro.ANALISANDO);

        return sinistroRepository.save(sinistro);

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
