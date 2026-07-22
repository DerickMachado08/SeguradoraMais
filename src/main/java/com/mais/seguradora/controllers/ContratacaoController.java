package com.mais.seguradora.controllers;

import com.mais.seguradora.Repositories.ApoliceRepository;
import com.mais.seguradora.Repositories.BemPatrimonialRepository;
import com.mais.seguradora.Repositories.ContratacaoRepository;
import com.mais.seguradora.enummerated.StatusContratacao;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.ApoliceService;
import com.mais.seguradora.services.ContratacaoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ContratacaoController {

    @Autowired
    private ApoliceService apoliceService;

    @Autowired
    private ContratacaoService contratacaoService;

    @Autowired
    private BemPatrimonialRepository bemPatrimonialRepository;

    @GetMapping("/cliente/apolices")
    public String minhasApolices(HttpSession session, Model model) {

        Cliente cliente =
                (Cliente) session.getAttribute("usuarioLogado");

        if(cliente == null){
            return "redirect:/login";
        }

        List<Contratacao> contratacoes =
                contratacaoService.buscarPorCliente(cliente);

        long pendentes = contratacoes.stream().filter(c -> c.getStatus() == StatusContratacao.PENDENTE).count();

        model.addAttribute("cliente", cliente);
        model.addAttribute("contratacoes", contratacoes);
        model.addAttribute("pendentes", pendentes);

        return "minhasApolices";
    }

    @GetMapping("/cliente/contratar")
    public String telaContratar(HttpSession session, Model model){

        Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");

        if (cliente == null) {
            return "redirect:/login";
        }

        List<Apolice> apolice = apoliceService.ListarApolice();

        model.addAttribute("cliente", cliente);

        model.addAttribute("apolices", apolice);

        return "contratarSeguro";
    }

    @PostMapping("/cliente/contratar")
    public String contratarSeguro(
            @RequestParam("idApolice") Integer idApolice,
            HttpSession session){

        Cliente cliente =
                (Cliente) session.getAttribute("usuarioLogado");

        if(cliente == null){
            return "redirect:/login";
        }

        Apolice apolice = apoliceService.buscarPorId(idApolice);

        if (contratacaoService.jaPossui(cliente, apolice)) {
            return "redirect:/cliente/contratar?erro=duplicado";
        }

        Contratacao contratacao = new Contratacao();

        contratacao.setCliente(cliente);
        contratacao.setApolice(apolice);
        contratacao.setDataContratacao(LocalDate.now());

        contratacao.setStatus(StatusContratacao.PENDENTE);
        contratacaoService.salvar(contratacao);

        return "redirect:/cliente/contratacao/" + contratacao.getId() + "/bem";
    }

    @PostMapping("/corretor/emitir-apolice")
    public String emitirApolice(@RequestParam("idContratacao") Integer idContratacao,
                                @RequestParam("valor") BigDecimal valor,
                                @RequestParam("parcelas") Integer parcelas, HttpSession session){

        Corretor corretor = (Corretor) session.getAttribute("usuarioLogado");

        if (corretor == null){
            return "redirect:/login.html";
        }

        contratacaoService.emitirApolice(idContratacao, valor, parcelas);

        return "redirect:/dashboardCorretor";
    }

    @GetMapping("/cliente/contratacao/{id}/bem")
    public String telaDeCadastroBens(@PathVariable Integer id, Model model, HttpSession session){

        Cliente cliente =
                (Cliente) session.getAttribute("usuarioLogado");

        Contratacao contratacao = contratacaoService.buscarPorId(id);

        model.addAttribute("cliente", cliente);
        model.addAttribute("contratacao", contratacao);
        model.addAttribute("idContratacao", id);
        model.addAttribute("bem", new BemPatrimonial());

        return "cadastrarBem";
    }

    @PostMapping("/cliente/contratacao/bem")
    public String cadastrarBem(
            @RequestParam("idContratacao") Integer idContratacao,
            @ModelAttribute BemPatrimonial bem){

        Contratacao contratacao = contratacaoService.buscarPorId(idContratacao);

        bem.setContratacao(contratacao);

        bemPatrimonialRepository.save(bem);

        return "redirect:/cliente/apolices";
    }



}
