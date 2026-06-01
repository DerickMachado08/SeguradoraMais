package com.mais.seguradora.Repositories;

import com.mais.seguradora.model.MensagemContato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensContatoRepsitory extends JpaRepository<MensagemContato, Integer> {
}
