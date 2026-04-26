package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.Sinistro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SinistroRepository extends JpaRepository<Sinistro, Integer> {
}
