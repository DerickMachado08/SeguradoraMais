package com.mais.seguradora.controllers;

import com.mais.seguradora.model.Admin;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Corretor;
import com.mais.seguradora.services.AdminService;
import com.mais.seguradora.services.ClienteService;
import com.mais.seguradora.services.CorretorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/auten")
public class LoginController {
    @Autowired
    private ClienteService clienteservice;

    @Autowired
    private CorretorService corretorService;

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String senha, HttpSession session) {
        Cliente cliente = clienteservice.autenticar(email, senha);

        if (cliente != null) {
            session.setAttribute("usuarioLogado", cliente);

            return ResponseEntity.ok(
                    Map.of(
                            "perfil", "CLIENTE", "redirect", "/dashboardCliente"
                    )
            );
        }
        Corretor corretor = corretorService.autenticar(email, senha);

        if (corretor != null) {
            session.setAttribute("usuarioLogado", corretor);

            return ResponseEntity.ok(
                    Map.of(
                            "perfil", "CORRETOR",
                            "redirect", "/dashboardCorretor"
                    )
            );
        }

        Admin admin = adminService.autenticar(email, senha);

        if (admin != null) {
            session.setAttribute("usuarioLogado", admin);

            return ResponseEntity.ok(
                    Map.of(
                            "perfil", "ADMIN",
                            "redirect", "/dashboardAdmin"
                    )
            );
        }

        return ResponseEntity.badRequest()
                .body("Email ou senha inválidos");
    }

}
