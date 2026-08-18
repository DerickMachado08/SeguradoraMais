package com.mais.seguradora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Tipo do bem obrigatorio")
    @Column(name = "tipo_bem", length = 50, nullable = false)
    private String tipoBem;

    @NotBlank(message = "Descricao do bem obrigatoria")
    @Column(name = "descricao", length = Integer.MAX_VALUE, nullable = false)
    private String descricao;

    @NotNull(message = "Valor estimado obrigatorio")
    @DecimalMin(value = "1.00", message = "Valor estimado deve ser maior que zero")
    @Column(name = "valor_estimado", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorEstimado;

    @NotNull(message = "Data de aquisicao obrigatoria")
    @Column(name = "data_aquisicao", nullable = false)
    private LocalDate dataAquisicao;

    @ManyToOne
    @JoinColumn(name = "id_ClienteB")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_contratacao")
    private Contratacao contratacao;
}
