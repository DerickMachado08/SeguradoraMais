package com.mais.seguradora.services;

import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.BemPatrimonial;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Cobertura;
import com.mais.seguradora.model.Contratacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApolicePdfServiceTest {

    @Test
    void deveGerarPdfParaApoliceEmitidaDoClienteLogado() {
        ApolicePdfService service = new ApolicePdfService(null, null);

        Cliente cliente = cliente();
        Contratacao contratacao = contratacao(cliente);
        BemPatrimonial bem = bem(contratacao, cliente);

        byte[] pdf = service.montarDocumento(contratacao, bem);

        assertTrue(pdf.length > 1_000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(7);
        cliente.setNome("João da Silva");
        cliente.setCpf("123.456.789-00");
        cliente.setEmail("joao@example.com");
        cliente.setTelefone("(11) 99999-9999");
        cliente.setLogradouro("Rua Exemplo");
        cliente.setNumero("100");
        cliente.setBairro("Centro");
        cliente.setCidade("São Paulo");
        cliente.setEstado("SP");
        cliente.setCep("00000-000");
        return cliente;
    }

    private Contratacao contratacao(Cliente cliente) {
        Cobertura cobertura = new Cobertura();
        cobertura.setNome("Danos elétricos");
        cobertura.setDescricao("Proteção para equipamentos danificados.");

        Apolice apolice = new Apolice();
        apolice.setId(3);
        apolice.setNumeroApolice("AP20260001");
        apolice.setTipoApolice("Seguro Residencial");
        apolice.setCoberturas(List.of(cobertura));

        Contratacao contratacao = new Contratacao();
        contratacao.setId(42);
        contratacao.setCliente(cliente);
        contratacao.setApolice(apolice);
        contratacao.setStatus(StatusContratacao.APOLICE_EMITIDA);
        contratacao.setDataContratacao(LocalDate.of(2026, 10, 10));
        contratacao.setValorContratacao(new BigDecimal("1500.00"));
        return contratacao;
    }

    private BemPatrimonial bem(Contratacao contratacao, Cliente cliente) {
        BemPatrimonial bem = new BemPatrimonial();
        bem.setId(8);
        bem.setCliente(cliente);
        bem.setContratacao(contratacao);
        bem.setTipoBem("Imóvel");
        bem.setDescricao("Casa residencial com dois dormitórios");
        bem.setValorEstimado(new BigDecimal("350000.00"));
        bem.setDataAquisicao(LocalDate.of(2020, 3, 15));
        return bem;
    }
}
