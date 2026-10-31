package com.mais.seguradora.Repositories;

import com.mais.seguradora.enummerated.StatusPagamento;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import com.mais.seguradora.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {
    List<Pagamento> findByContratacao_Cliente_IdOrderByIdDesc(Integer clienteId);
    Optional<Pagamento> findByIdAndContratacao_Cliente_Id(Integer id, Integer clienteId);
    /** Projeção escalar evita carregar entidades antes de aguardar o lock da contratação. */
    @Query("select p.contratacao.id from pagamento p where p.id = :id and p.contratacao.cliente.id = :clienteId")
    Optional<Integer> findContratacaoForCliente(@Param("id") Integer id, @Param("clienteId") Integer clienteId);
    List<Pagamento> findDistinctByContratacao_Apolice_Corretores_IdOrderByIdDesc(Integer corretorId);
    List<Pagamento> findByContratacao_IdOrderByIdAsc(Integer contratacaoId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from pagamento p where p.contratacao.id = :contratacaoId order by p.id")
    List<Pagamento> findForMobileOperation(@Param("contratacaoId") Integer contratacaoId);

    boolean existsByContratacao_Id(Integer contratacaoId);
    List<Pagamento> findByContratacao_ClienteOrderByDataVencimentoDesc(Cliente cliente);

    Optional<Pagamento> findFirstByContratacao_ClienteAndStatusOrderByContratacao_DataContratacaoDesc(
            Cliente cliente,
            StatusPagamento status
    );

    Optional<Pagamento> findByContratacao_IdAndStatus(Integer idContratacao, StatusPagamento status);

    void deleteByContratacaoId(Integer idContratacao);

   List<Pagamento> findByStatus(StatusPagamento status);


}
