package com.mais.seguradora.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mensagens_contato")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MensagemContato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String email;
    private String telefone;
    private String assunto;



}
