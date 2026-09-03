package com.mais.seguradora.Repositories;

import com.mais.seguradora.enummerated.StatusPagamento;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {
    List<Pagamento> findByContratacao_ClienteOrderByDataVencimentoDesc(Cliente cliente);

    Optional<Pagamento> findByContratacao_IdAndStatus(Integer idContratacao, StatusPagamento status);

    void deleteByContratacaoId(Integer idContratacao);

   List<Pagamento> findByStatus(StatusPagamento status);
}
