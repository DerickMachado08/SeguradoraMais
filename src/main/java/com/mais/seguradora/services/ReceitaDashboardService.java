package com.mais.seguradora.services;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** O site e a API usam os mesmos pagamentos aprovados e a mesma janela de seis meses. */
@Service
@Transactional(readOnly = true)
public class ReceitaDashboardService {
    public record Mes(String mes, String rotulo, BigDecimal valor) { }
    public record Receita(BigDecimal totalRecebido, List<Mes> meses) { }
    private final PagamentoService pagamentos;
    public ReceitaDashboardService(PagamentoService pagamentos) { this.pagamentos = pagamentos; }
    public Receita resumo() {
        var aprovados = pagamentos.buscarPagamentosAprovados();
        BigDecimal total = aprovados.stream().map(p -> p.getValor()).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var formatter = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("pt-BR"));
        var atual = YearMonth.now();
        List<Mes> meses = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth mes = atual.minusMonths(i);
            BigDecimal valor = aprovados.stream().filter(p -> p.getDataPagamento() != null)
                    .filter(p -> YearMonth.from(p.getDataPagamento()).equals(mes))
                    .map(p -> p.getValor()).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            String label = mes.format(formatter);
            label = label.substring(0, 1).toUpperCase(Locale.forLanguageTag("pt-BR")) + label.substring(1).replace(".", "");
            meses.add(new Mes(mes.toString(), label, valor));
        }
        return new Receita(total, List.copyOf(meses));
    }
}
