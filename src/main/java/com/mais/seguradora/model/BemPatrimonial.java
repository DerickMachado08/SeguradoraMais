package com.mais.seguradora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @PastOrPresent(message = "A data de aquisição não pode ser futura.")
    @NotNull(message = "Data de aquisicao obrigatoria")
    @Column(name = "data_aquisicao", nullable = false)
    private LocalDate dataAquisicao;

    @AssertTrue(message = "A data de aquisição deve ser a partir de 01/01/2018.")
    public boolean isDataAquisicaoValida() {
        if (dataAquisicao == null) {
            return true;
        }

        return !dataAquisicao.isBefore(LocalDate.of(2018, 1, 1));
    }

    @ManyToOne
    @JoinColumn(name = "id_ClienteB")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_contratacao")
    private Contratacao contratacao;
}
