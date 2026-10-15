package com.mais.seguradora.controllers;

import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.dto.ClienteAreaDtos.Erro;
import com.mais.seguradora.dto.CorretorAreaDtos.Preco;
import com.mais.seguradora.exception.CorretorAreaException;
import com.mais.seguradora.model.Corretor;
import com.mais.seguradora.services.CorretorAreaService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@RequestMapping("/api/corretor")
public class CorretorMobileController {
    private final CorretorAreaService service;
    public CorretorMobileController(CorretorAreaService service) { this.service = service; }
    @GetMapping("/dashboard") public ResponseEntity<?> dashboard(HttpServletRequest r) { return ok(service.dashboard(corretor(r))); }
    @GetMapping("/solicitacoes") public ResponseEntity<?> solicitacoes(HttpServletRequest r) { return ok(service.solicitacoes(corretor(r))); }
    @GetMapping("/solicitacoes/{id}") public ResponseEntity<?> detalhe(HttpServletRequest r, @PathVariable Integer id) { return ok(service.detalhe(corretor(r), id)); }
    @GetMapping("/apolices") public ResponseEntity<?> apolices(HttpServletRequest r) { return ok(service.apolices(corretor(r))); }
    @PutMapping("/solicitacoes/{id}/preco") public ResponseEntity<?> preco(HttpServletRequest r, @PathVariable Integer id,
            @RequestHeader(name = "X-Session-Token", required = false) String token, @Valid @RequestBody Preco pedido) {
        return ok(service.definirPreco(operacao(r, token), id, pedido));
    }
    @PostMapping("/solicitacoes/{id}/emitir") public ResponseEntity<?> emitir(HttpServletRequest r, @PathVariable Integer id,
            @RequestHeader(name = "X-Session-Token", required = false) String token, @Valid @RequestBody Preco pedido) {
        return ok(service.emitir(operacao(r, token), id, pedido));
    }
    @PostMapping("/solicitacoes/{id}/recusar") public ResponseEntity<?> recusar(HttpServletRequest r, @PathVariable Integer id,
            @RequestHeader(name = "X-Session-Token", required = false) String token) {
        return ok(service.recusar(operacao(r, token), id));
    }
    private static Integer operacao(HttpServletRequest r, String token) {
        Integer id = corretor(r);
        if (!SessionSecurity.matches(r.getSession(false), token)) throw new CorretorAreaException(403, "TOKEN_INVALIDO", "Entre novamente para continuar.");
        return id;
    }
    private static Integer corretor(HttpServletRequest r) {
        HttpSession session = r.getSession(false);
        Object user = session == null ? null : session.getAttribute(SessionSecurity.USER_ATTRIBUTE);
        if (user == null) throw new CorretorAreaException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
        if (!(user instanceof Corretor c)) throw new CorretorAreaException(403, "PERFIL_NAO_PERMITIDO", "Esta área é exclusiva para corretores.");
        if (c.getId() == null || c.getId() <= 0) throw new CorretorAreaException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
        return c.getId();
    }
    private static ResponseEntity<?> ok(Object value) { return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value); }
    @ExceptionHandler(CorretorAreaException.class) public ResponseEntity<?> erro(CorretorAreaException e) {
        return ResponseEntity.status(e.status()).cacheControl(CacheControl.noStore()).body(new Erro(e.codigo(), e.getMessage(), Map.of()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validacao(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new Erro("DADOS_INVALIDOS", "Confira o preço informado.", Map.of("valor", "Confira este campo.")));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> corpoInvalido() {
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new Erro("DADOS_INVALIDOS", "Confira os dados informados.", Map.of()));
    }
    @ExceptionHandler(Exception.class) public ResponseEntity<?> indisponivel() {
        return ResponseEntity.status(503).cacheControl(CacheControl.noStore()).body(new Erro("INDISPONIVEL", "Não foi possível confirmar a operação. Atualize a solicitação antes de tentar novamente.", Map.of()));
    }
}
