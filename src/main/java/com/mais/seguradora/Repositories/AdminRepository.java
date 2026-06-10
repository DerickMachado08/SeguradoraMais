package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer>{
    Optional<Admin> findByEmailAndSenha(String email, String senha);
}
