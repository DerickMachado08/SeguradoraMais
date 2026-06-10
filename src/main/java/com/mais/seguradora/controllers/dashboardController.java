package com.mais.seguradora.controllers;

import com.mais.seguradora.model.Cliente;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class dashboardController {

    @GetMapping("/dashboardCliente")
    public String dashboardCliente(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("usuarioLogado");
        model.addAttribute("cliente", cliente);

        return "dashboardCliente";
    }

        @GetMapping("/dashboardCorretor")
        public String dashboardCorretor() {
            return "dashboardCorretor";
        }

        @GetMapping("/dashboardAdmin")
        public String dashboardAdmin() {
            return "dashboardAdmin";
        }
    }

