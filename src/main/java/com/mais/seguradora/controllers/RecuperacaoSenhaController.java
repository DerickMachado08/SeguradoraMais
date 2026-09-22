package com.mais.seguradora.controllers;

import com.mais.seguradora.model.RecuperacaoSenha;
import com.mais.seguradora.services.RecuperacaoSenhaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/senha")
public class RecuperacaoSenhaController {

    @Autowired
    private RecuperacaoSenhaService recuperacaoSenhaService;

    @GetMapping("/esqueci")
    public String esqueciSenha(Model model){
        model.addAttribute("tipoFluxo", "recuperacao");

        return "senha/esqueci-senha";
    }

    @PostMapping("/enviar")
    public String enviarCodigo(@RequestParam("email") String email, Model model){
        try {
          recuperacaoSenhaService.enviarCodigo(email);

          model.addAttribute("email", email);
            model.addAttribute("tipoFluxo", "recuperacao");

          return "senha/verificar-codigo";

        } catch (RuntimeException e){
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("tipoFluxo", "recuperacao");

            return "senha/esqueci-senha";
        }
    }

    @PostMapping("/verificar-codigo")
    public String verificarCodigo(@RequestParam("email") String email,
                                  @RequestParam("codigo") String codigo,
                                  Model model){
        try {
            recuperacaoSenhaService.verificarCodigo(
                    email,
                    codigo
            );

            model.addAttribute("email", email);
            model.addAttribute("tipoFluxo", "recuperacao");

            return "senha/nova-senha";

        } catch (RuntimeException e){

            model.addAttribute("erro", e.getMessage());

            model.addAttribute("email", email);
            model.addAttribute("tipoFluxo", "recuperacao");

            return "senha/verificar-codigo";
        }
    }

    @PostMapping("/alterar")
    public String alterarSenha(@RequestParam("email") String email,
                               @RequestParam("novaSenha") String novaSenha,
                               @RequestParam("confirmarSenha") String confirmarSenha,
                               Model model){

        try {
            recuperacaoSenhaService.alterarSenha(email, novaSenha, confirmarSenha);

            model.addAttribute("sucesso",
                    "Senha alterada com sucesso!");

            return "redirect:/login.html";

        } catch (RuntimeException e){
            model.addAttribute("erro", e.getMessage());

            model.addAttribute("email", email);
            model.addAttribute("tipoFluxo", "recuperacao");

            return "senha/nova-senha";
        }
    }

}
