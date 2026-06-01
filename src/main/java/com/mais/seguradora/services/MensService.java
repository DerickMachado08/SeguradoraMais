package com.mais.seguradora.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MensService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarContato(String nome, String email, String  telefone, String assunto){
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo("seguramais.corretora.seguros@gmail.com");

        mensagem.setSubject("Solicittação de mensagem de contato - seguradoraMais");

        mensagem.setText(
                "Nome: " + nome + "\nEmail: " + email + "\nTelefone: " + telefone + "\n\nMensagem:\n" + assunto
        );

        mailSender.send(mensagem);
    }
}