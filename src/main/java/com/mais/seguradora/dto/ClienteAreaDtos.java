package com.mais.seguradora.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Contratos explícitos: nenhuma entidade JPA ou dado de outro usuário é serializado. */
public final class ClienteAreaDtos {
    private ClienteAreaDtos() { }
    public record CoberturaResumo(String nome, String descricao, String tipo) { }
    public record Produto(Integer id, String tipo, String numero, String descricao,
                          List<CoberturaResumo> coberturas, boolean disponivel, String motivo) { }
    public record BemResumo(String tipo, String descricao, BigDecimal valorEstimado, LocalDate dataAquisicao) { }
    public record Solicitacao(Integer id, LocalDate dataSolicitacao, String status, Produto apolice,
                             BigDecimal valorSeguro, List<BemResumo> bens) { }
    public record Dashboard(long totalSolicitacoes, long emAndamento, long apolicesEmitidas,
                            List<Solicitacao> recentes) { }
    public record Erro(String codigo, String mensagem, Map<String, String> campos) { }

    public record NovaSolicitacao(
            @NotNull @Positive Integer apoliceId,
            @NotBlank @Size(min = 5, max = 2000) String descricao,
            @NotNull @DecimalMin("1.00") @DecimalMax("99999999.99")
            @Digits(integer = 8, fraction = 2) BigDecimal valorEstimado,
            @NotNull @PastOrPresent LocalDate dataAquisicao) {
        @AssertTrue(message = "Informe uma data de aquisição a partir de 01/01/2018.")
        public boolean isDataAquisicaoValida() {
            return dataAquisicao == null || !dataAquisicao.isBefore(LocalDate.of(2018, 1, 1));
        }
        @AssertTrue(message = "Descreva e identifique o bem com pelo menos 5 caracteres.")
        public boolean isDescricaoValida() { return descricao == null || descricao.trim().length() >= 5; }
        @Override public String toString() { return "NovaSolicitacao[conteudo omitido]"; }
    }
}
