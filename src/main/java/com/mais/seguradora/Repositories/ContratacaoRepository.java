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

public interface ContratacaoRepository extends JpaRepository<Contratacao, Integer> {

    List<Contratacao> findByCliente(Cliente cliente);

    List<Contratacao> findByClienteId(Integer idCliente);

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


}