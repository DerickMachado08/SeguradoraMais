package com.mais.seguradora.controllers;

import com.mais.seguradora.Repositories.ApoliceRepository;
import com.mais.seguradora.Repositories.ClienteRepository;
import com.mais.seguradora.enummerated.StatusApolice;
import com.mais.seguradora.model.Admin;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import com.mais.seguradora.model.Corretor;
import com.mais.seguradora.services.ApoliceService;
import com.mais.seguradora.services.ClienteService;
import com.mais.seguradora.services.ContratacaoService;
import com.mais.seguradora.services.CorretorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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
    private ApoliceRepository apoliceRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/dashboardCliente")
    public String dashboardCliente(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");

        if (cliente == null) {
            return "redirect:/login.html";
        }

        List<Contratacao> contratacoes = contratacaoService.buscarPorCliente(cliente);

        long totalApolicesAtivas = contratacoes.stream()
                .filter(contratacao -> contratacao.getApolice() != null)
                .filter(contratacao -> contratacao.getApolice().getStatus() == StatusApolice.ATIVA)
                .count();

        model.addAttribute("cliente", cliente);
        model.addAttribute("contratacoes", contratacoes);
        model.addAttribute("totalApolices", contratacoes.size());
        model.addAttribute("totalApolicesAtivas", totalApolicesAtivas);

        return "dashboardCliente";
    }

    @GetMapping("/dashboardCorretor")
    public String dashboardCorretor(HttpSession session, Model model) {
        Corretor corretor = (Corretor) session.getAttribute("usuarioLogado");

        if (corretor == null) {
            return "redirect:/login.html";
        }

        model.addAttribute("corretor", corretor);
        model.addAttribute("contratacoesPendentes", contratacaoService.contratacoesPendentes());

        return "dashboardCorretor";
    }

    @GetMapping("/dashboardAdmin")
    public String dashboardAdmin(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("usuarioLogado");

        if (admin == null) {
            return "redirect:/login.html";
        }

        model.addAttribute("admin", admin);
        model.addAttribute("clientes", clienteService.ListarRecentes());
        model.addAttribute("corretores", corretorService.ListarTodos());
        model.addAttribute("totalApolices", apoliceService.totalApolice());
        model.addAttribute("totalClientes", clienteService.totalClientes());

        return "dashboardAdmin";
    }
}
