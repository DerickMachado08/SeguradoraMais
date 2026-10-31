package com.mais.seguradora.controllers;

import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.dto.SessaoResponse;
import com.mais.seguradora.model.Admin;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.model.Corretor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auten")
public class SessaoController {
    @GetMapping("/sessao")
    public ResponseEntity<?> sessao(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute(SessionSecurity.USER_ATTRIBUTE);
        SessaoResponse response = null;
        if (user instanceof Cliente cliente) {
            response = new SessaoResponse("CLIENTE", new SessaoResponse.UsuarioResumo(cliente.getId(), cliente.getNome(), cliente.getEmail()), SessionSecurity.token(session));
        } else if (user instanceof Corretor corretor) {
            response = new SessaoResponse("CORRETOR", new SessaoResponse.UsuarioResumo(corretor.getId(), corretor.getNome(), corretor.getEmail()), SessionSecurity.token(session));
        } else if (user instanceof Admin admin) {
            response = new SessaoResponse("ADMIN", new SessaoResponse.UsuarioResumo(admin.getId(), admin.getName(), admin.getEmail()), SessionSecurity.token(session));
        }
        if (response == null) return ResponseEntity.status(401).cacheControl(CacheControl.noStore())
                .body(Map.of("erro", "Sessão inválida ou expirada"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request,
                                    @RequestHeader(name = "X-Session-Token", required = false) String token) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SessionSecurity.USER_ATTRIBUTE) != null) {
            if (!SessionSecurity.matches(session, token)) return ResponseEntity.status(403).cacheControl(CacheControl.noStore())
                    .body(Map.of("erro", "Não foi possível validar esta operação"));
            session.invalidate();
        } else if (session != null) {
            session.invalidate();
        }
        String path = request.getContextPath().isEmpty() ? "/" : request.getContextPath();
        ResponseCookie expired = ResponseCookie.from("JSESSIONID", "").path(path).httpOnly(true)
                .secure(request.isSecure()).sameSite("Lax").maxAge(Duration.ZERO).build();
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, expired.toString()).build();
    }
}
