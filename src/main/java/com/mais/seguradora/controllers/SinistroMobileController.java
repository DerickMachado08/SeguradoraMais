package com.mais.seguradora.controllers;

import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.dto.SinistroMobileDtos.Abertura;
import com.mais.seguradora.exception.ComplementoMobileException;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.SinistroMobileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SinistroMobileController extends ComplementoMobileErrors {
    private final SinistroMobileService service;
    public SinistroMobileController(SinistroMobileService s) { service=s; }
    @GetMapping("/cliente/sinistros/opcoes") public ResponseEntity<?> opcoes(HttpServletRequest r) { return ok(service.opcoes(usuario(r,false))); }
    @GetMapping("/cliente/sinistros") public ResponseEntity<?> cliente(HttpServletRequest r) { return ok(service.listarCliente(usuario(r,false))); }
    @GetMapping("/cliente/sinistros/{id}") public ResponseEntity<?> clienteDetalhe(HttpServletRequest r,@PathVariable Integer id) { return ok(service.detalheCliente(usuario(r,false),id)); }
    @GetMapping("/admin/sinistros") public ResponseEntity<?> admin(HttpServletRequest r) { return ok(service.admin(usuario(r,true))); }
    @GetMapping("/admin/sinistros/{id}") public ResponseEntity<?> adminDetalhe(HttpServletRequest r,@PathVariable Integer id) { return ok(service.detalheAdmin(usuario(r,true),id)); }
    @PostMapping("/cliente/sinistros") public ResponseEntity<?> abrir(HttpServletRequest r,@RequestHeader(name="X-Session-Token",required=false) String token,@Valid @RequestBody Abertura pedido) {
        Integer id=usuario(r,false);
        if(!SessionSecurity.matches(r.getSession(false),token)) throw new ComplementoMobileException(403,"Entre novamente para continuar.");
        return ok(service.abrir(id,pedido));
    }
    private static Integer usuario(HttpServletRequest r,boolean admin) {
        Object u=r.getSession(false)==null?null:r.getSession(false).getAttribute(SessionSecurity.USER_ATTRIBUTE);
        if(u==null) throw new ComplementoMobileException(401,"Entre novamente para continuar.");
        if(admin?!(u instanceof Admin):!(u instanceof Cliente)) throw new ComplementoMobileException(403,"Consulta não disponível para este perfil.");
        Integer id=admin?((Admin)u).getId():((Cliente)u).getId();
        if(id==null||id<=0) throw new ComplementoMobileException(401,"Entre novamente para continuar.");
        return id;
    }
}
