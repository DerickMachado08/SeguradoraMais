package com.mais.seguradora.model;

import com.mais.seguradora.enummerated.StatusApolice;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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
    @Column(name = "numero_apolice", length = 50, nullable = false, unique = true)
    private String numeroApolice;
    @Column(name = "tipo_apolice", length = 50, nullable = false)
    private String tipoApolice;
    @Column(name = "descricao", length = Integer.MAX_VALUE)
    private String descricao;
    @Enumerated(EnumType.STRING)
    private StatusApolice status;

    @ManyToOne
    @JoinColumn(name = "id_corre")
    private Corretor corretor;

    @ManyToMany
    @JoinTable(
            name = "apolice_cobertura",
            joinColumns = @JoinColumn(name = "id_apolice"),
            inverseJoinColumns = @JoinColumn(name = "id_cobertura")
    )
    private List<Cobertura> coberturaList;
}
