package com.mais.seguradora.Repositories;

import com.mais.seguradora.enummerated.StatusSinistro;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Sinistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SinistroRepository extends JpaRepository<Sinistro, Integer> {
  List<Sinistro> findByCliente_IdOrderByIdDesc(Integer clienteId);
  java.util.Optional<Sinistro> findByIdAndCliente_Id(Integer id, Integer clienteId);
  boolean existsByCliente_IdAndApolice_IdAndStatus(Integer clienteId, Integer apoliceId, StatusSinistro status);
  List<Sinistro> findByApolice(Apolice apolice);
  List<Sinistro> findByClienteOrderByDataSinsitroDesc(Cliente cliente);
  List<Sinistro> findByStatus(StatusSinistro status);

}
