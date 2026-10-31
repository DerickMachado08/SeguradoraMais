package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.BemPatrimonialRepository;
import com.mais.seguradora.Repositories.ContratacaoRepository;
import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.BemPatrimonial;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Cobertura;
import com.mais.seguradora.model.Contratacao;
import jakarta.transaction.Transactional;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.ColumnText;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ApolicePdfService {

    private static final Color AZUL = new Color(15, 23, 42);
    private static final Color AZUL_CLARO = new Color(241, 245, 249);
    private static final Color VERDE = new Color(22, 101, 52);
    private static final Color CINZA = new Color(100, 116, 139);
    private static final Color LINHA = new Color(226, 232, 240);
    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final ContratacaoRepository contratacaoRepository;
    private final BemPatrimonialRepository bemPatrimonialRepository;

    public ApolicePdfService(
            ContratacaoRepository contratacaoRepository,
            BemPatrimonialRepository bemPatrimonialRepository) {
        this.contratacaoRepository = contratacaoRepository;
        this.bemPatrimonialRepository = bemPatrimonialRepository;
    }

    @Transactional
    public byte[] gerar(Integer idContratacao, Cliente clienteLogado) {
        if (clienteLogado == null || clienteLogado.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente nao autenticado");
        }

        Contratacao contratacao = contratacaoRepository.findById(idContratacao)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Contratacao nao encontrada"
                ));

        if (contratacao.getCliente() == null
                || !clienteLogado.getId().equals(contratacao.getCliente().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Esta apolice nao pertence ao cliente logado"
            );
        }

        if (contratacao.getStatus() != StatusContratacao.APOLICE_EMITIDA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O PDF fica disponivel somente depois da emissao da apolice"
            );
        }

        BemPatrimonial bem = bemPatrimonialRepository
                .findByContratacaoId(contratacao.getId())
                .orElse(null);

        return montarDocumento(contratacao, bem);
    }

    byte[] montarDocumento(Contratacao contratacao, BemPatrimonial bem) {
        ByteArrayOutputStream arquivo = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 42, 42, 42, 36);

        try {
            PdfWriter writer = PdfWriter.getInstance(documento, arquivo);
            writer.setPageEvent(new RodapePagina());
            documento.addTitle("Apólice " + contratacao.getApolice().getNumeroApolice());
            documento.addAuthor("Seguradora Mais");
            documento.addSubject("Documento da apólice emitida");
            documento.open();

            adicionarCabecalho(documento);
            adicionarSecao(documento, "DADOS DA APOLICE", dadosApolice(contratacao));
            adicionarSecao(documento, "DADOS DO SEGURADO", dadosCliente(contratacao.getCliente()));
            adicionarBem(documento, bem);
            adicionarCoberturas(documento, contratacao.getApolice());
            adicionarRodape(documento);
        } catch (DocumentException e) {
            throw new IllegalStateException("Nao foi possivel gerar o PDF da apolice", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }

        return arquivo.toByteArray();
    }

    private void adicionarCabecalho(Document documento) throws DocumentException {
        PdfPTable cabecalho = new PdfPTable(new float[]{3.2f, 1.4f});
        cabecalho.setWidthPercentage(100);
        cabecalho.setSpacingAfter(20);

        PdfPCell marca = celulaSemBorda();
        Paragraph titulo = new Paragraph("SEGURADORA MAIS", fonte(20, Font.BOLD, AZUL));
        titulo.setSpacingAfter(4);
        marca.addElement(titulo);
        marca.addElement(new Paragraph("Documento da contratação", fonte(10, Font.NORMAL, CINZA)));

        PdfPCell status = celulaSemBorda();
        status.setBackgroundColor(new Color(220, 252, 231));
        status.setPadding(10);
        status.setVerticalAlignment(Element.ALIGN_MIDDLE);
        status.setHorizontalAlignment(Element.ALIGN_CENTER);
        status.addElement(paragrafoAlinhado("APÓLICE EMITIDA", fonte(10, Font.BOLD, VERDE), Element.ALIGN_CENTER));

        cabecalho.addCell(marca);
        cabecalho.addCell(status);
        documento.add(cabecalho);
    }

    private PdfPTable dadosApolice(Contratacao contratacao) {
        Apolice apolice = contratacao.getApolice();
        PdfPTable tabela = tabelaDados();
        adicionarLinha(tabela, "Número da apólice", texto(apolice.getNumeroApolice()));
        adicionarLinha(tabela, "Código da contratação", texto(contratacao.getId()));
        adicionarLinha(tabela, "Tipo", texto(apolice.getTipoApolice()));
        adicionarLinha(tabela, "Status", "APÓLICE EMITIDA");
        adicionarLinha(tabela, "Data da contratação", formatarData(contratacao.getDataContratacao()));
        adicionarLinha(tabela, "Valor contratado", formatarMoeda(contratacao.getValorContratacao()));
        return tabela;
    }

    private PdfPTable dadosCliente(Cliente cliente) {
        PdfPTable tabela = tabelaDados();
        adicionarLinha(tabela, "Nome", texto(cliente.getNome()));
        adicionarLinha(tabela, "CPF", mascararCpf(cliente.getCpf()));
        adicionarLinha(tabela, "E-mail", texto(cliente.getEmail()));
        adicionarLinha(tabela, "Telefone", texto(cliente.getTelefone()));
        adicionarLinha(tabela, "Endereço", montarEndereco(cliente));
        adicionarLinha(tabela, "Cidade/Estado", texto(cliente.getCidade()) + "/" + texto(cliente.getEstado()));
        adicionarLinha(tabela, "CEP", texto(cliente.getCep()));
        return tabela;
    }

    private void adicionarBem(Document documento, BemPatrimonial bem) throws DocumentException {
        PdfPTable tabela = tabelaDados();
        if (bem == null) {
            adicionarLinha(tabela, "Bem segurado", "Informacao nao disponivel");
        } else {
            adicionarLinha(tabela, "Tipo do bem", texto(bem.getTipoBem()));
            adicionarLinha(tabela, "Descrição", texto(bem.getDescricao()));
            adicionarLinha(tabela, "Valor estimado", formatarMoeda(bem.getValorEstimado()));
            adicionarLinha(tabela, "Data de aquisição", formatarData(bem.getDataAquisicao()));
        }
        adicionarSecao(documento, "BEM SEGURADO", tabela);
    }

    private void adicionarCoberturas(Document documento, Apolice apolice) throws DocumentException {
        tituloSecao(documento, "COBERTURAS CONTRATADAS");

        List<Cobertura> coberturas = apolice.getCoberturas();
        if (coberturas == null || coberturas.isEmpty()) {
            Paragraph vazio = new Paragraph("Nenhuma cobertura informada.", fonte(10, Font.NORMAL, CINZA));
            vazio.setSpacingAfter(16);
            documento.add(vazio);
            return;
        }

        PdfPTable tabela = new PdfPTable(1);
        tabela.setWidthPercentage(100);
        tabela.setSpacingAfter(16);

        for (Cobertura cobertura : coberturas) {
            PdfPCell celula = new PdfPCell();
            celula.setBorder(Rectangle.BOTTOM);
            celula.setBorderColor(LINHA);
            celula.setPadding(9);
            celula.addElement(new Paragraph("• " + texto(cobertura.getNome()), fonte(10, Font.BOLD, AZUL)));
            if (cobertura.getDescricao() != null && !cobertura.getDescricao().isBlank()) {
                celula.addElement(new Paragraph(cobertura.getDescricao(), fonte(9, Font.NORMAL, CINZA)));
            }
            tabela.addCell(celula);
        }

        documento.add(tabela);
    }

    private void adicionarSecao(Document documento, String titulo, PdfPTable tabela) throws DocumentException {
        tituloSecao(documento, titulo);
        tabela.setSpacingAfter(16);
        documento.add(tabela);
    }

    private void tituloSecao(Document documento, String titulo) throws DocumentException {
        Paragraph paragrafo = new Paragraph(titulo, fonte(11, Font.BOLD, AZUL));
        paragrafo.setSpacingBefore(4);
        paragrafo.setSpacingAfter(8);
        documento.add(paragrafo);
    }

    private void adicionarRodape(Document documento) throws DocumentException {
        Paragraph linha = new Paragraph("Documento gerado em "
                + LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).format(DATA_HORA),
                fonte(8, Font.NORMAL, CINZA));
        linha.setSpacingBefore(8);
        linha.setAlignment(Element.ALIGN_CENTER);
        documento.add(linha);

    }

    private PdfPTable tabelaDados() {
        PdfPTable tabela = new PdfPTable(new float[]{1.35f, 2.65f});
        tabela.setWidthPercentage(100);
        return tabela;
    }

    private void adicionarLinha(PdfPTable tabela, String rotulo, String valor) {
        PdfPCell chave = new PdfPCell(new Phrase(rotulo, fonte(9, Font.BOLD, AZUL)));
        chave.setBackgroundColor(AZUL_CLARO);
        configurarCelula(chave);

        PdfPCell conteudo = new PdfPCell(new Phrase(valor, fonte(9, Font.NORMAL, Color.DARK_GRAY)));
        configurarCelula(conteudo);

        tabela.addCell(chave);
        tabela.addCell(conteudo);
    }

    private void configurarCelula(PdfPCell celula) {
        celula.setBorderColor(LINHA);
        celula.setPadding(8);
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
    }

    private PdfPCell celulaSemBorda() {
        PdfPCell celula = new PdfPCell();
        celula.setBorder(Rectangle.NO_BORDER);
        return celula;
    }

    private Paragraph paragrafoAlinhado(String texto, Font fonte, int alinhamento) {
        Paragraph paragrafo = new Paragraph(texto, fonte);
        paragrafo.setAlignment(alinhamento);
        return paragrafo;
    }

    private Font fonte(float tamanho, int estilo, Color cor) {
        return FontFactory.getFont(FontFactory.HELVETICA, tamanho, estilo, cor);
    }

    private String formatarMoeda(BigDecimal valor) {
        return valor == null ? "Nao informado" : NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    private String formatarData(LocalDate data) {
        return data == null ? "Nao informada" : data.format(DATA);
    }

    private String montarEndereco(Cliente cliente) {
        return texto(cliente.getLogradouro()) + ", " + texto(cliente.getNumero())
                + " - " + texto(cliente.getBairro());
    }

    private String mascararCpf(String cpf) {
        if (cpf == null) {
            return "Nao informado";
        }
        String numeros = cpf.replaceAll("\\D", "");
        if (numeros.length() != 11) {
            return "***.***.***-**";
        }
        return "***.***.***-" + numeros.substring(9);
    }

    private String texto(Object valor) {
        return valor == null || valor.toString().isBlank() ? "Nao informado" : valor.toString();
    }

    private class RodapePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Phrase pagina = new Phrase(
                    "Seguradora Mais  •  Documento eletrônico  •  Página " + writer.getPageNumber(),
                    fonte(8, Font.NORMAL, CINZA)
            );
            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    pagina,
                    (document.left() + document.right()) / 2,
                    document.bottom() - 18,
                    0
            );
        }
    }
}
