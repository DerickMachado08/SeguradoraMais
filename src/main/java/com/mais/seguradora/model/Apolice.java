package com.mais.seguradora.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "apolice")
@Table(name = "apolice")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Apolice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_apolice", nullable = false)
    private Integer id;
    @Column(name = "numero_apolice", length = 50)
    private String numeroApolice;
    @Column(name = "tipo_apolice", length = 50)
    private String tipoApolice;
    @Column(name = "descricao", length = Integer.MAX_VALUE)
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "id_corre")
    private Corretor corretor;
}
