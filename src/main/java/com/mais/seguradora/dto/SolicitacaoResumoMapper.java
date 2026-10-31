package com.mais.seguradora.dto;

import com.mais.seguradora.dto.ClienteAreaDtos.*;
import com.mais.seguradora.model.*;
import java.util.List;
import java.util.Objects;

/** Projeções explícitas compartilhadas; nunca serializa entidades ou credenciais. */
public final class SolicitacaoResumoMapper {
    private SolicitacaoResumoMapper() { }
    public static Solicitacao resumo(Contratacao c) {
        List<BemResumo> bens = c.getBensPatriomiais() == null ? List.of() : c.getBensPatriomiais().stream()
                .filter(b -> b.getCliente() != null && c.getCliente() != null
                        && Objects.equals(b.getCliente().getId(), c.getCliente().getId()))
                .map(b -> new BemResumo(b.getTipoBem(), b.getDescricao(), b.getValorEstimado(), b.getDataAquisicao())).toList();
        return new Solicitacao(c.getId(), c.getDataContratacao(), c.getStatus() == null ? null : c.getStatus().name(),
                c.getApolice() == null ? null : produto(c.getApolice(), false, null), c.getValorContratacao(), bens);
    }
    public static Produto produto(Apolice a, boolean disponivel, String motivo) {
        List<CoberturaResumo> coberturas = a.getCoberturas() == null ? List.of() : a.getCoberturas().stream()
                .map(c -> new CoberturaResumo(c.getNome(), c.getDescricao(), c.getTipoCobertura())).toList();
        return new Produto(a.getId(), a.getTipoApolice(), a.getNumeroApolice(), a.getDescricao(), coberturas, disponivel, motivo);
    }
}
