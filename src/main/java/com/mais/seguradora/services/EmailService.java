package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.MensContatoRepsitory;
import com.mais.seguradora.model.MensagemContato;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private MensContatoRepsitory mensContatoRepsitory;

    @Autowired
    private JavaMailSender mailSender;

    public MensagemContato salvar(MensagemContato mensagemContato){
        return mensContatoRepsitory.save(mensagemContato);
    }

    public void enviarContato(String nome, String email, String  telefone, String assunto){
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo("seguramais.corretora.seguros@gmail.com");

        mensagem.setSubject("Solicittação de mensagem de contato - seguradoraMais");

        mensagem.setText(
                "Nome: " + nome + "\nEmail: " + email + "\nTelefone: " + telefone + "\n\nMensagem:\n" + assunto
        );

        mailSender.send(mensagem);
    }

    public void enviarCodigoRecuperacao(String email, String codigo){
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo(email);

        mensagem.setSubject("Recuperação de Senha - SEGURADORA +");

        mensagem.setText("Olá!\n\n" +
        "Você solicitou a recuperação da sua senha.\n\n" +
                "Seu código de recuperação é: " + codigo + "\n\n" +
                "Este código expira em 15 minutos.\n\n" +
                "Se você não solicitou esta alteração, ignore este e-mail.");

        mailSender.send(mensagem);
    }


}