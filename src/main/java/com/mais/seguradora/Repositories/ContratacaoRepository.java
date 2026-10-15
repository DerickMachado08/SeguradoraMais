package com.mais.seguradora.Repositories;

import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import com.mais.seguradora.model.Corretor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface ContratacaoRepository extends JpaRepository<Contratacao, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from contratacao c where c.id = :id and c.cliente.id = :clienteId")
    Optional<Contratacao> findForPagamento(@Param("id") Integer id, @Param("clienteId") Integer clienteId);

    List<Contratacao> findDistinctByApolice_Corretores_Id(Integer corretorId);

    Optional<Contratacao> findByIdAndApolice_Corretores_Id(Integer id, Integer corretorId);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from contratacao c where c.id = :id and c.apolice.id in "
            + "(select a.id from apolice a join a.corretores r where r.id = :corretorId)")
    Optional<Contratacao> findForCorretorOperation(@Param("id") Integer id, @Param("corretorId") Integer corretorId);

    List<Contratacao> findByStatusIn(Collection<StatusContratacao> status);

    List<Contratacao> findByCliente(Cliente cliente);

    List<Contratacao> findByClienteId(Integer idCliente);

    Optional<Contratacao> findByIdAndClienteId(Integer id, Integer idCliente);

    List<Contratacao> findByStatusAndApoliceCorretores(
            StatusContratacao status,
            Corretor corretor
    );

    boolean existsByClienteAndApolice(
            Cliente cliente,
            Apolice apolice
    );

    List<Contratacao> findByClienteAndApolice_TipoApoliceAndStatusNot(
            Cliente cliente,
            String tipoApolice,
            StatusContratacao status
    );

    Optional<Contratacao> findFirstByClienteAndApoliceAndStatus(
            Cliente cliente,
            Apolice apolice,
            StatusContratacao status
    );

    Optional<Contratacao> findFirstByClienteAndApolice(
            Cliente cliente,
            Apolice apolice
    );



}
