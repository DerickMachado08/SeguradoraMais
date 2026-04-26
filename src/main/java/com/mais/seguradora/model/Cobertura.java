package com.mais.seguradora.model;

import jakarta.persistence.*;

@Entity(name = "cobertura")
@Table(name = "cobertura")
public class Cobertura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cobertura", nullable = false)
    private Integer id;
    @Column(name = "nome", length = 100)
    private String nome;
    @Column(name = "descricao", length = Integer.MAX_VALUE)
    private String descricao;
    @Column(name = "tipo_cobertura", length = 50)
    private String tipoCobertura;

    public Cobertura() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoCobertura() {
        return tipoCobertura;
    }

    public void setTipoCobertura(String tipoCobertura) {
        this.tipoCobertura = tipoCobertura;
    }
}
