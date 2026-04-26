package com.mais.seguradora.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "sinistro")
@Table(name = "sinistro")
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

    public Sinistro() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataSinsitro() {
        return dataSinsitro;
    }

    public void setDataSinsitro(LocalDate dataSinsitro) {
        this.dataSinsitro = dataSinsitro;
    }

    public BigDecimal getValorPrejuizo() {
        return valorPrejuizo;
    }

    public void setValorPrejuizo(BigDecimal valorPrejuizo) {
        this.valorPrejuizo = valorPrejuizo;
    }

    @ManyToOne
    @JoinColumn(name = "id_ApoliceS")
    private Apolice apolice;
}
