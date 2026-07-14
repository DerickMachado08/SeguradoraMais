package com.mais.seguradora.model;

import com.mais.seguradora.enummerated.StatusPagamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "pagamento")
@Table(name = "pagamento")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento", nullable = false)
    private Integer id;
    @Column(name = "quantidade_parcela", nullable = false)
    private Integer quantidadeParcela;
    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;
    @Column(name = "forma_pagamento", length = 20)
    private String formaPagamento;
    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;
    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    @ManyToOne
    @JoinColumn(name = "id_contratacao")
    private Contratacao contratacao;
}
