package com.mais.seguradora.controllers;

import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.dto.ClienteAreaDtos.Erro;
import com.mais.seguradora.dto.PagamentoMobileDtos.Confirmacao;
import com.mais.seguradora.exception.PagamentoMobileException;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.PagamentoMobileService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/api")
public class PagamentoMobileController {
    private final PagamentoMobileService service;
    public PagamentoMobileController(PagamentoMobileService service) { this.service = service; }
    @GetMapping("/cliente/pagamentos") public ResponseEntity<?> cliente(HttpServletRequest r) { return ok(service.cliente(usuario(r, Cliente.class))); }
    @GetMapping("/cliente/pagamentos/{id}") public ResponseEntity<?> detalhe(HttpServletRequest r, @PathVariable Integer id) { return ok(service.detalhe(usuario(r, Cliente.class), id)); }
    @GetMapping("/corretor/pagamentos") public ResponseEntity<?> corretor(HttpServletRequest r) { return ok(service.corretor(usuario(r, Corretor.class))); }
    @GetMapping("/admin/pagamentos") public ResponseEntity<?> admin(HttpServletRequest r) { return ok(service.admin(usuario(r, Admin.class))); }
    @PostMapping("/cliente/pagamentos/{id}/pagar") public ResponseEntity<?> pagar(HttpServletRequest r, @PathVariable Integer id,
            @RequestHeader(name = "X-Session-Token", required = false) String token, @Valid @RequestBody Confirmacao pedido) {
        Integer clienteId = usuario(r, Cliente.class);
        if (!SessionSecurity.matches(r.getSession(false), token)) throw new PagamentoMobileException(403, "TOKEN_INVALIDO", "Entre novamente para continuar.");
        return ok(service.pagar(clienteId, id, pedido));
    }
    private static Integer usuario(HttpServletRequest r, Class<?> perfil) {
        var session = r.getSession(false);
        Object user = session == null ? null : session.getAttribute(SessionSecurity.USER_ATTRIBUTE);
        if (user == null) throw new PagamentoMobileException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
        if (!perfil.isInstance(user)) throw new PagamentoMobileException(403, "PERFIL_NAO_PERMITIDO", "Esta consulta não está disponível para seu perfil.");
        Integer id = user instanceof Cliente c ? c.getId() : user instanceof Corretor c ? c.getId() : ((Admin) user).getId();
        if (id == null || id <= 0) throw new PagamentoMobileException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
        return id;
    }
    private static ResponseEntity<?> ok(Object body) { return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body); }
    @ExceptionHandler(PagamentoMobileException.class) public ResponseEntity<?> erro(PagamentoMobileException e) {
        return ResponseEntity.status(e.status()).cacheControl(CacheControl.noStore()).body(new Erro(e.codigo(), e.getMessage(), Map.of()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validacao() {
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new Erro("DADOS_INVALIDOS", "Confira o valor confirmado.", Map.of("valor", "Confira este campo.")));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> corpoInvalido() {
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new Erro("DADOS_INVALIDOS", "Confira os dados informados.", Map.of()));
    }
    @ExceptionHandler(Exception.class) public ResponseEntity<?> indisponivel() {
        return ResponseEntity.status(503).cacheControl(CacheControl.noStore()).body(new Erro("INDISPONIVEL", "Pagamento não confirmado. Atualize o histórico antes de tentar novamente.", Map.of()));
    }
}
