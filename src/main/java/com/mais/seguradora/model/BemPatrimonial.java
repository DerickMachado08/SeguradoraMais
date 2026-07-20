package com.mais.seguradora.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "bem_patrimonial")
@Table(name = "bem_patrimonial")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BemPatrimonial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bempatri", nullable = false)
    private Integer id;
    @Column(name = "tipo_bem", length = 50)
    private String tipoBem;
    @Column(name = "descricao", length = Integer.MAX_VALUE)
    private String descricao;
    @Column(name = "valor_estimado", precision = 10, scale = 2)
    private BigDecimal valorEstimado;
    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;

    @ManyToOne
    @JoinColumn(name = "id_ClienteB")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_contratacao")
    private Contratacao contratacao;
}
