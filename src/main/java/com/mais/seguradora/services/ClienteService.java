package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.BemPatrimonialRepository;
import com.mais.seguradora.Repositories.ClienteRepository;
import com.mais.seguradora.Repositories.ContratacaoRepository;
import com.mais.seguradora.Repositories.PagamentoRepository;
import com.mais.seguradora.exception.*;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private ContratacaoRepository contratacaoRepository;

    @Autowired
    private BemPatrimonialRepository bemPatrimonialRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existsByEmail;

    public Cliente cadastrar(Cliente cliente){
        if(repository.existsByCpf(cliente.getCpf())){
            throw new RecursoDuplicadoException("Cpf ja cadastrado!");
        }
        if (repository.existsByEmail(cliente.getEmail())){
            throw new RecursoDuplicadoException("Email ja cadastrado!");
        }
        if(cliente.getDataNascimento().isAfter(LocalDate.now().minusYears(18))){
            throw new MaiorIdadeException("por favor informar sua data de nascimento correta");
        }

        if(cliente.getDataNascimento().isBefore(LocalDate.now().minusYears(110))){
            throw new MenorIdadeException("por favor informar sua data de nascimento correta");
        }

        return repository.save(cliente);
    }

    public Cliente autenticar(String email, String senha) {
        Cliente cliente = repository.findByEmail(email).orElse(null);

        if (cliente != null &&
                passwordEncoder.matches(senha, cliente.getSenha())) {

            return cliente;
        }
        return null;
    }

    public List<Cliente> listarTodos(){
        return repository.findAll();
    }

    public List<Cliente> ListarRecentes(){
        return repository.findTop5ByOrderByIdDesc();
    }

    public Long totalClientes(){
        return repository.count();
    }

    public Cliente buscar(Integer id){
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Id não encontrado"));
    }

    @Transactional
    public void deletarConta(Integer id, String email, String senha) {

        Cliente cliente = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cliente não encontrado"));

        if (!cliente.getEmail().equals(email)) {
            throw new RecursoNullException("Email incorreto");
        }

        if (!cliente.getSenha().equals(senha)) {
            throw new RecursoNullException("Senha incorreta");
        }

        List<Contratacao> contratacoes =
                contratacaoRepository.findByClienteId(id);
        for (Contratacao contratacao : contratacoes) {
            pagamentoRepository.deleteByContratacaoId(
                    contratacao.getId()
            );
        }

        contratacaoRepository.deleteAll(contratacoes);

        bemPatrimonialRepository.deleteByClienteId(id);

        repository.delete(cliente);
    }
    public Cliente atualizar(Integer id, Cliente cliente) {
        Cliente existente = buscar(id);
        Cliente cliente1 = Cliente.builder()
                .nome(cliente.getNome())
                .build();
        return repository.save(cliente1);
    }
}