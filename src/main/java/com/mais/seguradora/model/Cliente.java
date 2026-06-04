package com.mais.seguradora.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

@Entity(name = "cliente")
@Table(name = "cliente")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente", nullable = false)
    private Integer id;

    @NotBlank(message = "Nome obrigatório")
    @Column(name = "nome", nullable = false, length = 80)
    private String nome;

    @NotBlank(message = "CPF obrigatório")
    @CPF(message = "CPF inválido")
    @Column(name = "cpf", nullable = false, length = 14, unique = true)
    private String cpf;

    @NotNull(message = "Data de nascimento obrigatório")
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @NotBlank(message = "Email obrigatório")
    @Email(message = "Email inválido")
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Telefone obrigatório")
    @Column(name = "telefone", length = 20, nullable = false)
    private String telefone;

    @NotBlank(message = "Logradouro obrigatório")
    @Column(name = "logradouro", length = 100, nullable = false)
    private String logradouro;

    @NotBlank(message = "Estado obrigatório")
    @Column(name = "estado", length = 2, nullable = false)
    private String estado;

    @NotBlank(message = "Cidade obrigatório")
    @Column(name = "cidade", length = 50, nullable = false)
    private String cidade;

    @NotBlank(message = "Bairro obrigatório")
    @Column(name = "bairro", length = 50, nullable = false)
    private String bairro;

    @NotBlank(message = "Nuumero obrigatório")
    @Column(name = "numero", length = 10, nullable = false)
    private String numero;

    @NotBlank(message = "cep obrigatório")
    @Column(name = "cep", length = 9, nullable = false)
    private String cep;

    @NotBlank(message = "Senha obrigatória")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "A senha deve conter letra maiúscula, minúscula, número e caractere especial"
    )
    @Column(name = "senha", length = 26, nullable = false)
    private String senha;



}
