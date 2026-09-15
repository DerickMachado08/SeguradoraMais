package com.mais.seguradora.controllers;

import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.enummerated.StatusPagamento;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import com.mais.seguradora.model.Pagamento;
import com.mais.seguradora.services.ApoliceService;
import com.mais.seguradora.services.ContratacaoService;
import com.mais.seguradora.services.PagamentoService;
import com.mais.seguradora.services.SinistroService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/sinistro")
public class SinistroController {

    @Autowired
    private SinistroService sinistroService;

    @Autowired
    private ApoliceService apoliceService;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private ContratacaoService contratacaoService;

    @GetMapping("/novo")
    public String novoSinistro(
            @RequestParam("idApolice") Integer idApolice,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");

        if (cliente == null) {
            return "redirect:/login.html";
        }

        Apolice apolice = apoliceService.buscarPorId(idApolice);

        // Busca SOMENTE a contratação ativa
        Optional<Contratacao> contratacaoOptional =
                contratacaoService.buscarPorClienteEApoliceAtiva(
                        cliente,
                        apolice
                );

        // Não encontrou contratação ativa
        if (contratacaoOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Você não possui uma contratação ativa para esta apólice."
            );

            return "redirect:/cliente/apolices";
        }

        // Agora pegamos a contratação correta
        Contratacao contratacao = contratacaoOptional.get();

        // Verifica se o pagamento dessa contratação está aprovado
        if (!pagamentoService.pagamentoAprovado(contratacao.getId())) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Você só poderá abrir um sinistro após o pagamento da apólice."
            );

            return "redirect:/cliente/apolices";
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("apolice", apolice);
        model.addAttribute("contratacao", contratacao);
        model.addAttribute("idApolice", idApolice);

        return "sinistro/novo";
    }

    @PostMapping("/abrir")
    public String abrirSinistro(
            @RequestParam("idApolice") Integer idApolice,
            @RequestParam("descricao") String descricao,
            @RequestParam("valorPrejuizo") BigDecimal valorPrejuizo,
            HttpSession session) {

        Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");

        if (cliente == null) {
            return "redirect:/login.html";
        }

        sinistroService.abrir(idApolice, descricao, valorPrejuizo, cliente
        );

        return "redirect:/dashboardCliente";
    }


}
