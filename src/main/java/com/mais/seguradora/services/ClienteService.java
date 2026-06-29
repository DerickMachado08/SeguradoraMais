package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.ClienteRepository;
import com.mais.seguradora.exception.CpfDuplicadoException;
import com.mais.seguradora.exception.EmailDuplicadoException;
import com.mais.seguradora.exception.MaiorIdadeException;
import com.mais.seguradora.exception.MenorIdadeException;
import com.mais.seguradora.model.Cliente;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;

    public boolean existsByEmail;

    public Cliente cadastrar(Cliente cliente){
        if(repository.existsByCpf(cliente.getCpf())){
            throw new CpfDuplicadoException();
        }
        if (repository.existsByEmail(cliente.getEmail())){
            throw new EmailDuplicadoException();
        }
        if(cliente.getDataNascimento().isAfter(LocalDate.now().minusYears(18))){
            throw new MenorIdadeException();
        }

        if(cliente.getDataNascimento().isBefore(LocalDate.now().minusYears(110))){
            throw new MaiorIdadeException();
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
