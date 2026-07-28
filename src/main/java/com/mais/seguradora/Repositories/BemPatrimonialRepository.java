package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.BemPatrimonial;
import com.mais.seguradora.model.Contratacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BemPatrimonialRepository extends JpaRepository<BemPatrimonial, Integer> {

    Optional<BemPatrimonial> findByContratacaoId(Integer id);

}
