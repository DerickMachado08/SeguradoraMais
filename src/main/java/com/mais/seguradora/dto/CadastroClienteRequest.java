package com.mais.seguradora.dto;

import com.mais.seguradora.model.Cliente;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;

/** Entrada pública: não aceita identidade persistente ou privilégios do cliente. */
public record CadastroClienteRequest(
        @NotBlank(message = "Nome obrigatório")
        @Pattern(regexp = "^[A-Za-zÀ-ÿ\\s]{3,80}$", message = "Nome inválido") String nome,
        @NotBlank(message = "CPF obrigatório") @CPF(message = "CPF inválido")
        @Size(max = 14, message = "CPF inválido") String cpf,
        @NotNull(message = "Data de nascimento obrigatória")
        @PastOrPresent(message = "Data de nascimento não pode ser futura") LocalDate dataNascimento,
        @NotBlank(message = "Email obrigatório") @Email(message = "Email inválido")
        @Size(max = 100, message = "Email deve ter até 100 caracteres") String email,
        @NotBlank(message = "Telefone obrigatório") @Size(max = 20, message = "Telefone inválido") String telefone,
        @NotBlank(message = "Logradouro obrigatório") @Size(max = 100, message = "Logradouro deve ter até 100 caracteres") String logradouro,
        @NotBlank(message = "Estado obrigatório") @Pattern(regexp = "[A-Z]{2}", message = "Informe a sigla do estado") String estado,
        @NotBlank(message = "Cidade obrigatória") @Size(max = 50, message = "Cidade deve ter até 50 caracteres") String cidade,
        @NotBlank(message = "Bairro obrigatório") @Size(max = 50, message = "Bairro deve ter até 50 caracteres") String bairro,
        @NotBlank(message = "Número obrigatório") @Size(max = 4, message = "Número deve ter até 4 caracteres") String numero,
        @NotBlank(message = "CEP obrigatório") @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido") String cep,
        @NotBlank(message = "Senha obrigatória") @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$", message = "Use letra maiúscula, minúscula, número e um símbolo @$!%*?&") String senha) {

    public Cliente toEntity() {
        return Cliente.builder().nome(nome.trim()).cpf(cpf.trim()).dataNascimento(dataNascimento)
                .email(email.trim()).telefone(telefone.trim()).logradouro(logradouro.trim())
                .estado(estado).cidade(cidade.trim()).bairro(bairro.trim()).numero(numero.trim())
                .cep(cep.trim()).senha(senha).build();
    }

    @AssertTrue(message = "A senha deve ter no máximo 72 bytes")
    public boolean isSenhaTamanhoValido() {
        return senha == null || senha.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override public String toString() { return "CadastroClienteRequest[conteúdo omitido]"; }
}
