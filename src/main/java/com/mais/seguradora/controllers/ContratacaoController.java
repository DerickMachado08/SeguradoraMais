package com.mais.seguradora.controllers;

import com.mais.seguradora.Repositories.ApoliceRepository;
import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Contratacao;
import com.mais.seguradora.services.ContratacaoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ContratacaoController {

    @Autowired
    private ContratacaoService contratacaoService;

    @Autowired
    private ApoliceRepository apoliceRepository;

    @GetMapping("/cliente/apolices")
    public String minhasApolices(HttpSession session, Model model) {

        Cliente cliente =
                (Cliente) session.getAttribute("usuarioLogado");

        if(cliente == null){
            return "redirect:/login";
        }

        List<Contratacao> contratacoes =
                contratacaoService.buscarPorCliente(cliente);

        model.addAttribute("cliente", cliente);
        model.addAttribute("contratacoes", contratacoes);

        return "minhasApolices";
    }

    @GetMapping("/cliente/contratar")
    public String telaContratar(HttpSession session, Model model){

        Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");

        if (cliente == null) {
            return "redirect:/login";
        }

        model.addAttribute("cliente", cliente);

        model.addAttribute(
                "apolices",
                apoliceRepository.findAll()
        );

        return "contratarSeguro";
    }

    @PostMapping("/cliente/contratar")
    public String contratarSeguro(
            @RequestParam Integer idApolice,
            HttpSession session){

        Cliente cliente =
                (Cliente) session.getAttribute("usuarioLogado");

        if(cliente == null){
            return "redirect:/login";
        }

        Apolice apolice =
                apoliceRepository.findById(idApolice)
                        .orElseThrow();

        Contratacao contratacao = new Contratacao();

        contratacao.setCliente(cliente);
        contratacao.setApolice(apolice);
        contratacao.setDataContratacao(LocalDate.now());

        contratacaoService.salvar(contratacao);

        return "redirect:/cliente/apolices";
    }
}
