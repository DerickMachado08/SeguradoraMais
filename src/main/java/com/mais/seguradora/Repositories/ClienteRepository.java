package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    /** Serializa novas solicitações mobile do mesmo cliente sem alterar o esquema. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from cliente c where c.id = :id")
    Optional<Cliente> findForSolicitacao(@Param("id") Integer id);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Cliente> findByEmail(String email);
    List<Cliente> findTop5ByOrderByIdDesc();
}
