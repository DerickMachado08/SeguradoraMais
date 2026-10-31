package com.mais.seguradora.dto;

import com.mais.seguradora.services.ReceitaDashboardService.Receita;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
import java.util.List;

public final class AdminAreaDtos {
    private AdminAreaDtos() { }
    public record Dashboard(long totalClientes, long totalCorretores, long pendentes, long apolicesEmitidas, Receita receita) { }
    public record ClienteResumo(Integer id, String nome, String email, String telefone) { }
    public record Produto(Integer id, String tipo, String numero) { }
    public record CorretorResumo(Integer id, String nome, String email, String telefone, List<Produto> produtos) { }
    public record Solicitacao(ClienteAreaDtos.Solicitacao solicitacao, String clienteNome) { }
    public record NovoCorretor(
            @NotBlank @Size(max = 100) @Pattern(regexp = "^[A-Za-zÀ-ÿ\\s]{3,100}$") String nome,
            @NotBlank @CPF @Size(max = 14) String cpf,
            @NotBlank @Email @Size(max = 100) String email,
            @NotBlank @Pattern(regexp = "^(?=(?:[^0-9]*[0-9]){10,11}[^0-9]*$)[+()0-9 .-]{10,20}$") String telefone,
            @NotBlank @Size(min = 8, max = 72) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$") String senha,
            @NotNull @Positive Integer produtoId) {
        public NovoCorretor {
            nome = nome == null ? null : nome.trim();
            cpf = cpf == null ? null : cpf.trim();
            email = email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
            telefone = telefone == null ? null : telefone.trim();
        }
        @Override public String toString() { return "NovoCorretor[conteúdo omitido]"; }
    }
    /** Substituição explícita da carteira inteira por produto, inclusive lista vazia. */
    public record Vinculos(@NotNull @Size(max = 200) List<@NotNull @Positive Integer> produtoIds) { }
}
