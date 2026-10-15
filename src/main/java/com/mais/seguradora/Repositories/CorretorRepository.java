package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Corretor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CorretorRepository extends JpaRepository<Corretor, Integer> {
    boolean existsByEmailIgnoreCase(String email);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from corretor c where c.id = :id")
    Optional<Corretor> findForAdminOperation(@org.springframework.data.repository.query.Param("id") Integer id);
    Optional<Corretor> findByEmailAndSenha(String email, String senha);
    Optional<Corretor> findByEmail(String email);
    List<Corretor> findByApolices(Apolice apolice);
}
