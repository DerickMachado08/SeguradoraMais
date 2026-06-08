package com.mais.seguradora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Admin {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name = "id_admin", nullable = false)
    private Integer id;

    @NotBlank(message = "Email obrigatório")
    @Email(message = "Email inválido")
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Senha obrigatória")
    @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
            message = "A senha deve conter letra maiúscula, minúscula, número e caractere especial"
    )
    @Column(name = "senha", length = 26, nullable = false)
    private String senha;
}
