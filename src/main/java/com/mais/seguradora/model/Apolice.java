package com.mais.seguradora.model;

import jakarta.persistence.*;

@Entity(name = "apolice")
@Table(name = "apolice")
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

    public Apolice() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumeroApolice() {
        return numeroApolice;
    }

    public void setNumeroApolice(String numeroApolice) {
        this.numeroApolice = numeroApolice;
    }

    public String getTipoApolice() {
        return tipoApolice;
    }

    public void setTipoApolice(String tipoApolice) {
        this.tipoApolice = tipoApolice;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @ManyToOne
    @JoinColumn(name = "id_corre")
    private Corretor corretor;
}
