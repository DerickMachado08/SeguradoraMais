package com.mais.seguradora.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public final class CorretorAreaDtos {
    private CorretorAreaDtos() { }
    public record Solicitacao(ClienteAreaDtos.Solicitacao solicitacao, String clienteNome,
            boolean podeDefinirPreco, boolean podeEmitir, boolean podeRecusar) { }
    public record Dashboard(long totalSolicitacoes, long emAndamento, long apolicesEmitidas,
            List<Solicitacao> recentes) { }
    public record Preco(@NotNull @DecimalMin("1.00") @DecimalMax("99999999.99")
            @Digits(integer = 8, fraction = 2) BigDecimal valor) { }
}
