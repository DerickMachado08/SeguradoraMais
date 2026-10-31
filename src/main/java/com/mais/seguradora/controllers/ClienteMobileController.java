package com.mais.seguradora.controllers;

import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.dto.ClienteAreaDtos.*;
import com.mais.seguradora.exception.ClienteAreaException;
import com.mais.seguradora.model.Cliente;
import com.mais.seguradora.services.ClienteAreaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** API mobile aditiva. Não altera redirecionamentos e formulários do site. */
@RestController
@RequestMapping("/api/cliente")
public class ClienteMobileController {
    private final ClienteAreaService service;
    public ClienteMobileController(ClienteAreaService service) { this.service = service; }
    @GetMapping("/dashboard") public ResponseEntity<?> dashboard(HttpServletRequest request) { return ok(service.dashboard(cliente(request))); }
    @GetMapping("/catalogo") public ResponseEntity<?> catalogo(HttpServletRequest request) { return ok(service.catalogo(cliente(request))); }
    @GetMapping("/solicitacoes") public ResponseEntity<?> solicitacoes(HttpServletRequest request) { return ok(service.solicitacoes(cliente(request))); }
    @GetMapping("/solicitacoes/{id}") public ResponseEntity<?> detalhe(HttpServletRequest request, @PathVariable Integer id) { return ok(service.detalhe(cliente(request), id)); }
    @GetMapping("/apolices") public ResponseEntity<?> apolices(HttpServletRequest request) { return ok(service.apolices(cliente(request))); }
    @PostMapping("/solicitacoes") public ResponseEntity<?> solicitar(HttpServletRequest request,
            @RequestHeader(name = "X-Session-Token", required = false) String token, @Valid @RequestBody NovaSolicitacao pedido) {
        Integer id = cliente(request);
        if (!SessionSecurity.matches(request.getSession(false), token))
            throw new ClienteAreaException(403, "TOKEN_INVALIDO", "Sua sessão precisa ser confirmada. Entre novamente.");
        return ResponseEntity.status(201).cacheControl(CacheControl.noStore()).body(service.solicitar(id, pedido));
    }
    private static Integer cliente(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute(SessionSecurity.USER_ATTRIBUTE);
        if (user == null) throw new ClienteAreaException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
        if (!(user instanceof Cliente c)) throw new ClienteAreaException(403, "PERFIL_NAO_PERMITIDO", "Esta área é exclusiva para clientes.");
        if (c.getId() == null || c.getId() <= 0) throw new ClienteAreaException(401, "SESSAO_INVALIDA", "Entre novamente para continuar.");
        return c.getId();
    }
    private static ResponseEntity<?> ok(Object value) { return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(value); }
    @ExceptionHandler(ClienteAreaException.class) public ResponseEntity<?> erro(ClienteAreaException ex) {
        return ResponseEntity.status(ex.status()).cacheControl(CacheControl.noStore()).body(new Erro(ex.codigo(), ex.getMessage(), Map.of()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> campos.put(switch (e.getField()) {
            case "dataAquisicaoValida" -> "dataAquisicao";
            case "descricaoValida" -> "descricao";
            default -> e.getField();
        }, "Confira este campo."));
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new Erro("DADOS_INVALIDOS", "Confira os campos informados.", campos));
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> corpoInvalido() {
        return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new Erro("DADOS_INVALIDOS", "Confira os dados informados.", Map.of()));
    }
    @ExceptionHandler(Exception.class) public ResponseEntity<?> indisponivel() {
        return ResponseEntity.status(503).cacheControl(CacheControl.noStore()).body(new Erro("INDISPONIVEL", "Não foi possível concluir agora. Consulte suas solicitações antes de reenviar.", Map.of()));
    }
}
