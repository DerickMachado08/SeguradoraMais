package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    Optional<Cliente> findByEmail(String email);
    Optional<Cliente> findByEmailAndSenha(String email, String senha);
}
