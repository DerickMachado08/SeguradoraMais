package com.mais.seguradora.Repositories;

import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratacaoRepository extends JpaRepository<Contratacao, Integer> {
    List<Contratacao> findByCliente(Cliente cliente);

    List<Contratacao> findByStatus(StatusContratacao status);
}
