package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.RecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecuperacaoSenhaRepository extends JpaRepository<RecuperacaoSenha, Long> {
 Optional<RecuperacaoSenha> findByEmailAndCodigoAndUtilizadoFalse(String email, String codigo);

 Optional<RecuperacaoSenha> findFirstByEmailAndUtilizadoFalseOrderByIdDesc(String email);

}
