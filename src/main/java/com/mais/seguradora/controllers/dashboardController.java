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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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


    @Autowired
    private ReceitaDashboardService receitaDashboardService;

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

        var receita = receitaDashboardService.resumo();
        BigDecimal total = receita.totalRecebido();
        List<Sinistro> sinistros = sinistroService.buscarTodos();
        List<Contratacao> contratacoesAprovadas = contratacaoService.buscarContratacoesAprovadas();

        var receitaLabels = receita.meses().stream().map(ReceitaDashboardService.Mes::rotulo).toList();
        var receitaValores = receita.meses().stream().map(ReceitaDashboardService.Mes::valor).toList();

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
