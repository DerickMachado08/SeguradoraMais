package com.mais.seguradora.controllers;

import com.mais.seguradora.model.*;
import com.mais.seguradora.services.*;
import com.mais.seguradora.utils.DocumentUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class dashboardController {

    @Autowired
    private ApoliceService apoliceService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ContratacaoService contratacaoService;

    @Autowired
    private CorretorService corretorService;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private SinistroService sinistroService;


    @GetMapping("/dashboardCliente")
    public String dashboardCliente(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");

        if (cliente == null) {
            return "redirect:/login.html";
        }

        List<Contratacao> contratacoes = contratacaoService.buscarPorCliente(cliente);
        List<Sinistro> sinistros = sinistroService.buscarPorCliente(cliente);
        List<Pagamento> pagamentos = pagamentoService.buscarPorCliente(cliente);



        model.addAttribute("cliente", cliente);
        model.addAttribute("contratacoes", contratacoes);
        model.addAttribute("sinistros", sinistros);
        model.addAttribute("pagamentos", pagamentos);
        model.addAttribute("pagamentoPendente", pagamentoService.buscarUltimoPendente(cliente).orElse(null));
        model.addAttribute("cpfMascara", DocumentUtil.cpfMascara(cliente.getCpf()));
        model.addAttribute("totalApolices", contratacoes.size());
        model.addAttribute("pendentes", contratacaoService.contarPendentes(contratacoes));
        model.addAttribute("totalApolicesEmitidas", contratacaoService.totalApolicesEmitidas(contratacoes));
        model.addAttribute("totalPagamentosAprovados", pagamentoService.contarAprovados(cliente));
        model.addAttribute("totalPagamentosPendentes", pagamentoService.contarPendentes(cliente));
        model.addAttribute("totalSinistros", sinistros.size());

        return "cliente/dashboard/dashboard";
    }

    @GetMapping("/dashboardCorretor")
    public String dashboardCorretor(HttpSession session, Model model) {
        Corretor corretor = (Corretor) session.getAttribute("usuarioLogado");

        if (corretor == null) {
            return "redirect:/login.html";
        }

        model.addAttribute("corretor", corretor);
        model.addAttribute("contratacoesPendentes", contratacaoService.contratacoesPendentes(corretor));

        return "cliente/dashboard/dashboardCorretor";
    }

    @GetMapping("/dashboardAdmin")
    public String dashboardAdmin(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("usuarioLogado");

        if (admin == null) {
            return "redirect:/login.html";
        }

        BigDecimal total = pagamentoService.calcularTotalRecebido();
        List<Sinistro> sinistros = sinistroService.buscarTodos();
        List<Pagamento> pagamentosAprovados = pagamentoService.buscarPagamentosAprovados();
        List<Contratacao> contratacoesAprovadas = contratacaoService.buscarContratacoesAprovadas();

        DateTimeFormatter mesFormatter = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("pt-BR"));
        YearMonth mesAtual = YearMonth.now();
        List<String> receitaLabels = new ArrayList<>();
        List<BigDecimal> receitaValores = new ArrayList<>();

        for (int indice = 5; indice >= 0; indice--) {
            YearMonth mes = mesAtual.minusMonths(indice);
            BigDecimal valorDoMes = pagamentosAprovados.stream()
                    .filter(pagamento -> pagamento.getDataPagamento() != null)
                    .filter(pagamento -> YearMonth.from(pagamento.getDataPagamento()).equals(mes))
                    .map(Pagamento::getValor)
                    .filter(valor -> valor != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String label = mes.format(mesFormatter);
            receitaLabels.add(label.substring(0, 1).toUpperCase() + label.substring(1).replace(".", ""));
            receitaValores.add(valorDoMes);
        }

        Map<String, Long> segurosContratados = new LinkedHashMap<>();
        contratacoesAprovadas.stream()
                .filter(contratacao -> contratacao.getApolice() != null)
                .map(contratacao -> contratacao.getApolice().getTipoApolice())
                .filter(tipo -> tipo != null && !tipo.isBlank())
                .sorted()
                .forEach(tipo -> segurosContratados.merge(tipo, 1L, Long::sum));

        model.addAttribute("documentUtil", DocumentUtil.class);

        model.addAttribute("admin", admin);
        model.addAttribute("clientes", clienteService.ListarRecentes());
        model.addAttribute("corretores", corretorService.listarTodos());
        model.addAttribute("totalApolices", apoliceService.totalApolice());
        model.addAttribute("totalClientes", clienteService.totalClientes());
        model.addAttribute("totalCorretores", corretorService.totalCorretores());
        model.addAttribute("totalContratacoesAprovadas", contratacoesAprovadas.size());
        model.addAttribute("total", total);
        model.addAttribute("sinistros", sinistros);
        model.addAttribute("receitaLabels", receitaLabels);
        model.addAttribute("receitaValores", receitaValores);
        model.addAttribute("segurosLabels", new ArrayList<>(segurosContratados.keySet()));
        model.addAttribute("segurosValores", new ArrayList<>(segurosContratados.values()));

        return "cliente/dashboard/dashboardAdmin";
    }
}
