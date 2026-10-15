package com.mais.seguradora.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class PagamentoMobileDtos {
    private PagamentoMobileDtos() { }
    public record Resumo(Integer id, Integer contratacaoId, String seguro, String referenciaProduto,
            String status, String statusContratacao, BigDecimal valor, LocalDate dataPagamento,
            LocalDate dataVencimento, String formaPagamento, boolean ficticio, boolean podePagar,
            String motivo, String clienteNome) { }
    public record Confirmacao(@NotNull @DecimalMin("1.00") @DecimalMax("99999999.99")
            @Digits(integer = 8, fraction = 2) BigDecimal valor) { }
}
