package com.mais.seguradora.controllers;

import com.mais.seguradora.model.Admin;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Corretor;
import com.mais.seguradora.services.AdminService;
import com.mais.seguradora.services.ClienteService;
import com.mais.seguradora.services.CorretorService;
import com.mais.seguradora.config.SessionSecurity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.nio.charset.StandardCharsets;

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
    public ResponseEntity<?> login(@RequestParam("email") String email, @RequestParam("senha") String senha, HttpServletRequest request) {
        if (email.isBlank() || email.length() > 100 || senha.isEmpty()
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) return invalidLogin(request);
        Cliente cliente = clienteservice.autenticar(email, senha);

        if (cliente != null) {
            authenticateSession(request, cliente);

            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
                    Map.of(
                            "perfil", "CLIENTE", "redirect", "/dashboardCliente"
                    )
            );
        }
        Corretor corretor = corretorService.autenticar(email, senha);

        if (corretor != null) {
            authenticateSession(request, corretor);

            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
                    Map.of(
                            "perfil", "CORRETOR",
                            "redirect", "/dashboardCorretor"
                    )
            );
        }

        Admin admin = adminService.autenticar(email, senha);

        if (admin != null) {
            authenticateSession(request, admin);

            return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(
                    Map.of(
                            "perfil", "ADMIN",
                            "redirect", "/dashboardAdmin"
                    )
            );
        }

        return invalidLogin(request);
    }

    private ResponseEntity<?> invalidLogin(HttpServletRequest request) {
        HttpSession previous = request.getSession(false);
        if (previous != null) previous.invalidate();
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore())
                .body("Email ou senha inválidos");
    }

    private void authenticateSession(HttpServletRequest request, Object user) {
        HttpSession session = request.getSession();
        request.changeSessionId();
        session.setAttribute(SessionSecurity.USER_ATTRIBUTE, user);
        SessionSecurity.renewToken(session);
    }

}
