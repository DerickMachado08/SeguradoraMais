package com.mais.seguradora.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class SinistroMobileDtos {
    private SinistroMobileDtos() { }
    public record Opcao(Integer apoliceId, String seguro, String referenciaProduto, boolean elegivel, String motivo) { }
    public record Resumo(Integer id, Integer apoliceId, String seguro, String referenciaProduto,
            String descricao, LocalDate dataRegistro, BigDecimal valorPrejuizo, String status, String clienteNome) { }
    public record Abertura(@NotNull @Positive Integer apoliceId,
            @NotBlank @Size(min = 10, max = 2000) String descricao,
            @NotNull @DecimalMin("0.01") @DecimalMax("99999999.99") @Digits(integer = 8, fraction = 2) BigDecimal valorPrejuizo) {
        @JsonAnySetter public void desconhecido(String key, Object value) { throw new IllegalArgumentException("Campo não permitido"); }
    }
}
