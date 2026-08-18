package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ClienteRepository;
import com.mais.seguradora.exception.*;
import com.mais.seguradora.model.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

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
           return repository.findByEmailAndSenha(email,senha).orElse(null);
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

    public void deletar(Integer id){
        repository.deleteById(id);
    }

    public Cliente atualizar(Integer id, Cliente cliente) {
        Cliente existente = buscar(id);
        Cliente cliente1 = Cliente.builder()
                .nome(cliente.getNome())
                .build();
        return repository.save(cliente1);
    }
}
