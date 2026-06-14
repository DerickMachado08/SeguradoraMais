package com.mais.seguradora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mensagens_contato")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MensagemContato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Pattern(
            regexp = "^[A-Za-zÀ-ÿ\\s]{3,100}$",
            message = "Nome inválido"
    )
    private String name;

    @Email(message = "Email ivalido")
    private String email;

    @Pattern(
            regexp = "^\\([0-9]{2}\\)\\s[0-9]{5}-[0-9]{4}$"
    )
    private String telefone;

    @Size(min = 20, max = 500)
    private String assunto;



}
