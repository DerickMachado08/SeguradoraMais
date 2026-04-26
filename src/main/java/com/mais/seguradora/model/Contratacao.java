package com.mais.seguradora.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "contratacao")
@Table(name = "contratacao")
public class Contratacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contratacao", nullable = false)
    private Integer id;
    @Column(name = "data_contratacao")
    private LocalDate dataContratacao;
    @Column(name = "valor_contratacao", precision = 10, scale = 2)
    private BigDecimal valorContratacao;

    public Contratacao() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDataContratacao() {
        return dataContratacao;
    }

    public void setDataContratacao(LocalDate dataContratacao) {
        this.dataContratacao = dataContratacao;
    }

    public BigDecimal getValorContratacao() {
        return valorContratacao;
    }

    public void setValorContratacao(BigDecimal valorContratacao) {
        this.valorContratacao = valorContratacao;
    }

    @ManyToOne
    @JoinColumn(name = "id_c")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_a")
    private Apolice apolice;
}
