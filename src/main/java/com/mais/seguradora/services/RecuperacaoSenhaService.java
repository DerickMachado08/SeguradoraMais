package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ClienteRepository;
import com.mais.seguradora.Repositories.RecuperacaoSenhaRepository;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.RecuperacaoSenha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class RecuperacaoSenhaService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RecuperacaoSenhaRepository recuperacaoSenhaRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void enviarCodigo(String email) {

        Cliente cliente = clienteRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Email não encontrado"));


        String codigo = gerarCodigo();

        RecuperacaoSenha recuperacao =
                new RecuperacaoSenha();

        recuperacao.setEmail(email);
        recuperacao.setCodigo(codigo);
        recuperacao.setDataExpiracao(LocalDateTime.now().plusHours(15));

        recuperacao.setUtilizado(false);

        recuperacaoSenhaRepository.save(recuperacao);

        emailService.enviarCodigoRecuperacao(email, codigo);

    }

    private String gerarCodigo(){
        Random random = new Random();

        int numero = 100000 + random.nextInt(900000);

        return String.valueOf(numero);
    }

    public void verificarCodigo(String email, String codigo){

        RecuperacaoSenha recuperacao = recuperacaoSenhaRepository.
                findByEmailAndCodigoAndUtilizadoFalse(email, codigo).
                orElseThrow(()-> new RuntimeException("Codigo invalido"));

        if (recuperacao
                .getDataExpiracao().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Codigo expirado");
        }
    }

    public void alterarSenha(String email, String novaSenha, String confirmarSenha){
        if(!novaSenha.equals(confirmarSenha)){
            throw new RuntimeException("As senhas não são iguais");
        }

        RecuperacaoSenha recuperacaoSenha = recuperacaoSenhaRepository
                .findFirstByEmailAndUtilizadoFalseOrderByIdDesc(email)
                .orElseThrow(()-> new RuntimeException("Recuperação invalida"));

        if (recuperacaoSenha.getDataExpiracao().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Codigo expirado");
        }

        Cliente cliente = clienteRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("cliente não encontrado"));

        cliente.setSenha(passwordEncoder.encode(novaSenha));

        clienteRepository.save(cliente);

        recuperacaoSenha.setUtilizado(true);

        recuperacaoSenhaRepository.save(recuperacaoSenha);
    }
}
