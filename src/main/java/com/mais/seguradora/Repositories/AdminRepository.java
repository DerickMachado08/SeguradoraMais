package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Integer>{
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from Admin a where a.id = :id")
    Optional<Admin> findForPerfil(@org.springframework.data.repository.query.Param("id") Integer id);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Admin> findByEmailAndSenha(String email, String senha);
}
