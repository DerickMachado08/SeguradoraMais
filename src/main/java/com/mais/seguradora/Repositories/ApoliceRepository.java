package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.Apolice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApoliceRepository extends JpaRepository<Apolice, Integer> {
    /** Ordem única de locks para cadastro e troca de vínculos mobile, sem alteração de esquema. */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from apolice a order by a.id")
    java.util.List<Apolice> findAllForAdminOperation();
}
