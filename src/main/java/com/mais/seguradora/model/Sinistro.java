package com.mais.seguradora.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "sinistro")
@Table(name = "sinistro")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sinistro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sinistro", nullable = false)
    private Integer id;
    @Column(name = "descricao", length = Integer.MAX_VALUE)
    private String descricao;
    @Column(name = "data_sinsitro")
    private LocalDate dataSinsitro;
    @Column(name = "valor_prejuizo", precision = 10, scale = 2)
    private BigDecimal valorPrejuizo;

    @ManyToOne
    @JoinColumn(name = "id_ApoliceS")
    private Apolice apolice;
}
