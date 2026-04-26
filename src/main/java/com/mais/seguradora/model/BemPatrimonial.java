package com.mais.seguradora.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "bem_patrimonial")
@Table(name = "bem_patrimonial")
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

    public BemPatrimonial() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipoBem() {
        return tipoBem;
    }

    public void setTipoBem(String tipoBem) {
        this.tipoBem = tipoBem;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(BigDecimal valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(LocalDate dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    @ManyToOne
    @JoinColumn(name = "id_ClienteB")
    private Cliente cliente;
}
