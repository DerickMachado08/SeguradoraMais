package com.mais.seguradora.controllers;

import com.mais.seguradora.services.SinistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/sinistro")
public class SinistroController {

    @Autowired
    private SinistroService sinistroService;

    @GetMapping("/novo")
    public String novoSinistro(@RequestParam("idApolice") Integer idApolice, Model model){

        model.addAttribute("idApolice", idApolice);

        return "sinistro/novo";

    }

    @PostMapping("/abrir")
    public String abrirSinistro(@RequestParam("idApolice") Integer idApolice,
                                @RequestParam("descricao") String descricao,
                                @RequestParam("valorPrejuizo")BigDecimal valorPrejuizo){

        sinistroService.abrir(idApolice, descricao, valorPrejuizo);

        return "redirect:/dashboardCliente";
    }


}
