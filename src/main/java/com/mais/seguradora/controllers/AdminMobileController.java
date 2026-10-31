package com.mais.seguradora.controllers;

import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.dto.ClienteAreaDtos.Erro;
import com.mais.seguradora.dto.AdminAreaDtos.*;
import com.mais.seguradora.exception.AdminAreaException;
import com.mais.seguradora.model.Admin;
import com.mais.seguradora.services.AdminAreaService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/api/admin")
public class AdminMobileController {
    private final AdminAreaService service;
    public AdminMobileController(AdminAreaService service) { this.service = service; }
    @GetMapping("/dashboard") public ResponseEntity<?> dashboard(HttpServletRequest r) { return ok(service.dashboard(admin(r))); }
    @GetMapping("/clientes") public ResponseEntity<?> clientes(HttpServletRequest r) { return ok(service.clientes(admin(r))); }
    @GetMapping("/corretores") public ResponseEntity<?> corretores(HttpServletRequest r) { return ok(service.corretores(admin(r))); }
    @GetMapping("/produtos") public ResponseEntity<?> produtos(HttpServletRequest r) { return ok(service.produtos(admin(r))); }
    @GetMapping("/solicitacoes") public ResponseEntity<?> solicitacoes(HttpServletRequest r) { return ok(service.solicitacoes(admin(r), false)); }
    @GetMapping("/solicitacoes/{id}") public ResponseEntity<?> detalhe(HttpServletRequest r, @PathVariable Integer id) { return ok(service.detalhe(admin(r), id)); }
    @GetMapping("/apolices") public ResponseEntity<?> apolices(HttpServletRequest r) { return ok(service.solicitacoes(admin(r), true)); }
    @PostMapping("/corretores") public ResponseEntity<?> cadastrar(HttpServletRequest r,
            @RequestHeader(name = "X-Session-Token", required = false) String token, @Valid @RequestBody NovoCorretor pedido) {
        return ResponseEntity.status(201).cacheControl(CacheControl.noStore()).body(service.cadastrar(operacao(r, token), pedido));
    }
    @PutMapping("/corretores/{id}/produtos") public ResponseEntity<?> vincular(HttpServletRequest r, @PathVariable Integer id,
            @RequestHeader(name = "X-Session-Token", required = false) String token, @Valid @RequestBody Vinculos pedido) {
        return ok(service.vincular(operacao(r, token), id, pedido));
    }
    private static Integer operacao(HttpServletRequest r, String token) {
        Integer id = admin(r);
        if (!SessionSecurity.matches(r.getSession(false), token)) throw new AdminAreaException(403, "TOKEN_INVALIDO", "Entre novamente para continuar.");
        return id;
    }
    private static Integer admin(HttpServletRequest r) {
        var session = r.getSession(false);
        Object user = session == null ? null : session.getAttribute(SessionSecurity.USER_ATTRIBUTE);
        if (user == null) throw new AdminAreaException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
        if (!(user instanceof Admin a)) throw new AdminAreaException(403, "PERFIL_NAO_PERMITIDO", "Esta área é exclusiva para administradores.");
        if (a.getId() == null || a.getId() <= 0) throw new AdminAreaException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
        return a.getId();
    }
    private static ResponseEntity<?> ok(Object value) { return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value); }
    @ExceptionHandler(AdminAreaException.class) public ResponseEntity<?> erro(AdminAreaException e) {
        return ResponseEntity.status(e.status()).cacheControl(CacheControl.noStore()).body(new Erro(e.codigo(), e.getMessage(), Map.of()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        Set<String> permitidos = Set.of("nome", "cpf", "email", "telefone", "senha", "produtoId", "produtoIds");
        e.getBindingResult().getFieldErrors().forEach(f -> { if (permitidos.contains(f.getField())) campos.put(f.getField(), "Confira este campo."); });
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new Erro("DADOS_INVALIDOS", "Confira os dados informados.", campos));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> corpoInvalido() {
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new Erro("DADOS_INVALIDOS", "Confira os dados informados.", Map.of()));
    }
    @ExceptionHandler(Exception.class) public ResponseEntity<?> indisponivel() {
        return ResponseEntity.status(503).cacheControl(CacheControl.noStore()).body(new Erro("INDISPONIVEL", "Operação não confirmada. Atualize a lista antes de tentar novamente.", Map.of()));
    }
}
