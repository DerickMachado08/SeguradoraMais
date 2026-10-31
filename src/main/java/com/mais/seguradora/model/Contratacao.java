package com.mais.seguradora.model;

import com.mais.seguradora.enummerated.StatusContratacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "contratacao")
@Table(name = "contratacao")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Contratacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contratacao", nullable = false)
    private Integer id;
    @Column(name = "data_contratacao")
    private LocalDate dataContratacao;
    @Column(name = "valor_contratacao", precision = 10, scale = 2)
    private BigDecimal valorContratacao;
    @Enumerated(EnumType.STRING)
    private StatusContratacao status;

    @ManyToOne
    @JoinColumn(name = "id_c")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_a")
    private Apolice apolice;

    @OneToMany(mappedBy = "contratacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BemPatrimonial> bensPatriomiais = new ArrayList<>();
}
