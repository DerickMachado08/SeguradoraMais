package com.mais.seguradora.controllers;

import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.exception.RecursoDuplicadoException;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.BemPatrimonial;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import com.mais.seguradora.model.Corretor;
import com.mais.seguradora.services.ApoliceService;
import com.mais.seguradora.services.BemPatrimonialService;
import com.mais.seguradora.services.ContratacaoService;
import com.mais.seguradora.services.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ContratacaoController {

    @Autowired
    private ApoliceService apoliceService;

    @Autowired
    private ContratacaoService contratacaoService;

    @Autowired
    private BemPatrimonialService bemPatrimonialService;

    @GetMapping("/cliente/apolices")
    public String minhasApolices(
            HttpSession session,
            Model model) {

        Cliente cliente =
                (Cliente) session.getAttribute("usuarioLogado");

        List<Contratacao> contratacoes =
                contratacaoService.buscarPorCliente(cliente);

        model.addAttribute("cliente", cliente);
        model.addAttribute("contratacoes", contratacoes);
        model.addAttribute("pendentes", contratacaoService.contarPendentes(contratacoes));
        model.addAttribute("totalApolicesEmitidas", contratacaoService.totalApolicesEmitidas(contratacoes));

        return "minhasApolices";
    }

    @GetMapping("/cliente/contratar")
    public String telaContratar(
            @RequestParam(value = "erro", required = false) String erro,
            HttpSession session,
            Model model) {

        Cliente cliente =
                (Cliente) session.getAttribute("usuarioLogado");

        List<Apolice> apolices =
                apoliceService.ListarApolice();

        List<Contratacao> contratacoesCliente =
                contratacaoService.buscarPorCliente(cliente);

        model.addAttribute("cliente", cliente);
        model.addAttribute("apolices", apolices);
        model.addAttribute("contratacoesCliente", contratacoesCliente);
        model.addAttribute("erro", erro);
        model.addAttribute(
                "tiposBloqueados",
                contratacaoService.buscarTiposBloqueados(cliente)
        );

        return "contratarSeguro";
    }

    @PostMapping("/cliente/contratar")
    public String contratarSeguro(
            @RequestParam("idApolice") Integer idApolice,
            HttpSession session) {

        Cliente cliente =
                (Cliente) session.getAttribute("usuarioLogado");

        Apolice apolice =
                apoliceService.buscarPorId(idApolice);

        try {

            Contratacao contratacao =
                    contratacaoService.iniciarContratacao(
                            cliente,
                            apolice
                    );

            return "redirect:/cliente/contratacao/"
                    + contratacao.getId()
                    + "/bem";

        } catch (RecursoDuplicadoException e) {

            return "redirect:/cliente/contratar?erro=duplicado";
        }
    }

    @GetMapping("/cliente/contratacao/{id}/bem")
    public String telaDeCadastroBens(
            @PathVariable("id") Integer id,
            Model model,
            HttpSession session) {

        Cliente cliente =
                (Cliente) session.getAttribute("usuarioLogado");

        Contratacao contratacao =
                contratacaoService.buscarPorId(id);

        if (!contratacao.getCliente().getId()
                .equals(cliente.getId())) {

            return "redirect:/cliente/apolices";
        }

        model.addAttribute("cliente", cliente);
        model.addAttribute("contratacao", contratacao);
        model.addAttribute("idContratacao", id);
        model.addAttribute("bem", new BemPatrimonial());

        return "cadastrarBem";
    }

    @PostMapping("/cliente/contratacao/bem")
    public String cadastrarBem(
            @RequestParam("idContratacao") Integer idContratacao,
            @ModelAttribute("bem") BemPatrimonial bem,
            HttpSession session,
            Model model) {

        Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");

        try {
            contratacaoService.cadastrarBemDaContratacao(
                    idContratacao,
                    cliente,
                    bem
            );

            return "redirect:/cliente/apolices";

        } catch (RuntimeException e) {

            Contratacao contratacao =
                    contratacaoService.buscarPorId(idContratacao);

            model.addAttribute("cliente", cliente);
            model.addAttribute("contratacao", contratacao);
            model.addAttribute("idContratacao", idContratacao);
            model.addAttribute("erro", e.getMessage());

            return "cadastrarBem";
        }
    }

    @PostMapping("/cliente/contratacao/{id}/deletar")
    public String deletar(
            @PathVariable("id") Integer id,
            HttpSession session) {

        Cliente cliente =
                (Cliente) session.getAttribute("usuarioLogado");

        contratacaoService.deletar(id);

        return "redirect:/cliente/apolices";
    }

    @PostMapping("/corretor/emitir-apolice")
    public String emitirApolice(
            @RequestParam("idContratacao") Integer idContratacao,
            @RequestParam("valor") BigDecimal valor,
            @RequestParam("parcelas") Integer parcelas,
            HttpSession session) {

        Corretor corretor =
                (Corretor) session.getAttribute("usuarioLogado");

        contratacaoService.emitirApolice(idContratacao, valor, parcelas
        );

        return "redirect:/dashboardCorretor";
    }

    @GetMapping("/corretor/contratacao/{id}/bens")
    public String verBens(
            @PathVariable("id") Integer id,
            Model model) {

        Contratacao contratacao =
                contratacaoService.buscarPorId(id);

        model.addAttribute("contratacao", contratacao);

        model.addAttribute(
                "bem",
                bemPatrimonialService
                        .buscarOpcionalPorContratacao(id)
                        .orElse(null)
        );

        return "bens";
    }

    @PostMapping("/corretor/contratacao/{id}/deletar")
    public String deletarContratacaoCorretor(
            @PathVariable("id") Integer id) {

        contratacaoService.deletarNegadaCorretor(id);

        return "redirect:/dashboardCorretor";
    }


}