package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.MensContatoRepsitory;
import com.mais.seguradora.model.MensagemContato;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private MensContatoRepsitory mensContatoRepsitory;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    @Value("${app.mail.contact-to:${spring.mail.username}}")
    private String destinatarioContato;

    public MensagemContato salvar(MensagemContato mensagemContato){
        return mensContatoRepsitory.save(mensagemContato);
    }

    public void enviarContato(String nome, String email, String  telefone, String assunto){
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setFrom(remetente);
        mensagem.setTo(destinatarioContato);
        mensagem.setReplyTo(email);

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

    public void enviarContratacaoAprovada(String email){
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo(email);

        mensagem.setSubject("Contratação aprovada - Seguradora+");

        mensagem.setText( "Olá!\n\n" +
                "Sua contratação foi aprovada pela Seguradora+.\n\n" +
                "Agora sua apólice está disponível no sistema.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Seguradora+");

        mailSender.send(mensagem);
    }

    public void enviarContratacaoNegada(String email){
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo(email);

        mensagem.setSubject("Contratação negada - Seguradora+");

        mensagem.setText(  "Olá!\n\n" +
                "Informamos que sua solicitação de contratação não foi aprovada.\n\n" +
                "Caso tenha dúvidas, entre em contato com a Seguradora+.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Seguradora+");

        mailSender.send(mensagem);
    }

    public void sinistroAprovada(String email){
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo(email);

        mensagem.setSubject("Sinistro aprovado - Seguradora+");

        mensagem.setText( "Olá!\n\n" +
                "Seu sinistro foi aprovado pela Seguradora+.\n\n" +
                "Agora você pode observar que o sinistro está aprovado no sistema.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Seguradora+");

        mailSender.send(mensagem);
    }

    public void sinistroNegado(String email){
        SimpleMailMessage mensagem = new SimpleMailMessage();

        mensagem.setTo(email);

        mensagem.setSubject("Sinistro negado - Seguradora+");

        mensagem.setText(  "Olá!\n\n" +
                "Informamos que sua solicitação do sinistro não foi aprovada.\n\n" +
                "Caso tenha dúvidas, entre em contato com a Seguradora+.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Seguradora+");

        mailSender.send(mensagem);
    }


}
