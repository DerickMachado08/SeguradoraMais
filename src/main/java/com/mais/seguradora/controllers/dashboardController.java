package com.mais.seguradora.controllers;

import com.mais.seguradora.Repositories.ClienteRepository;
import com.mais.seguradora.enummerated.StatusApolice;
import com.mais.seguradora.model.Admin;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import com.mais.seguradora.services.AdminService;
import com.mais.seguradora.services.ClienteService;
import com.mais.seguradora.services.ContratacaoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class dashboardController {

    @Autowired
    private ContratacaoService contratacaoService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ClienteService clienteService;

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
    public String dashboardCorretor() {
        return "dashboardCorretor";
    }

    @GetMapping("/dashboardAdmin")
    public String dashboardAdmin(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("usuarioLogado");
        List<Cliente> clientes = clienteRepository.findTop5ByOrderByIdDesc();

        model.addAttribute("admin", admin);
        model.addAttribute("clientes", clientes);

        return "dashboardAdmin";
    }
}
