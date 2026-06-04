package com.mais.seguradora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "corretor")
@Table(name = "corretor")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Corretor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_corretor", nullable = false)
    private Integer id;
    @Column(name = "nome", length = 100)
    private String nome;
    @Column(name = "cpf", length = 14)
    private String cpf;
    @Column(name = "email", length = 100)
    private String email;
    @Column(name = "telefone", length = 20)
    private String telefone;
    @NotBlank
    @Column(name = "senha", length = 50)
    private String senha;



}
