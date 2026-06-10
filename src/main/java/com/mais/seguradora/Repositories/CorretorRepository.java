package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.Corretor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CorretorRepository extends JpaRepository<Corretor, Integer> {
    Optional<Corretor> findByEmailAndSenha(String email, String senha);
}
