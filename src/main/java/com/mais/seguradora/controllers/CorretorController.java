package com.mais.seguradora.controllers;

import com.mais.seguradora.model.Apolice;
import com.mais.seguradora.model.Corretor;
import com.mais.seguradora.services.ApoliceService;
import com.mais.seguradora.services.CorretorService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/corretor")
public class CorretorController {

    @Autowired
    private CorretorService corretorService;

    @Autowired
    private ApoliceService apoliceService;

    @GetMapping("/cadastrar")
    public String telaCadastro(Model model){
        model.addAttribute("corretor", new Corretor());

        List<Apolice> apolices = apoliceService.ListarApolice();

        model.addAttribute("apolices", apolices);

        return "cadastrarCorretor";
    }


    @PostMapping("/cadastrar")
    public String cadastrar(@Valid @ModelAttribute Corretor corretor,
                            @RequestParam("idApolice") Integer idApolice){
      corretorService.cadastrar(corretor, idApolice);
      return "redirect:/dashboardAdmin";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public void deletar(@PathVariable("id") Integer id) {

        corretorService.deletarCorretor(id);
    }
}
