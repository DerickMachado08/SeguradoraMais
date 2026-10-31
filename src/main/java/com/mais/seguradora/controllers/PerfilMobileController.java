package com.mais.seguradora.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mais.seguradora.config.SessionSecurity;
import com.mais.seguradora.dto.PerfilMobileDtos.*;
import com.mais.seguradora.exception.ComplementoMobileException;
import com.mais.seguradora.model.*;
import com.mais.seguradora.services.PerfilMobileService;
import jakarta.servlet.http.*;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilMobileController extends ComplementoMobileErrors {
    private final PerfilMobileService service;
    public PerfilMobileController(PerfilMobileService s) { service=s; }
    @GetMapping public ResponseEntity<?> consultar(HttpServletRequest r) { Identidade i=identidade(r);return ok(service.consultar(i.perfil(),i.id())); }
    @PutMapping public ResponseEntity<?> salvar(HttpServletRequest r,@RequestHeader(name="X-Session-Token",required=false) String token,@RequestBody JsonNode body) {
        HttpSession session=r.getSession(false);
        Object original=session==null?null:session.getAttribute(SessionSecurity.USER_ATTRIBUTE);
        Identidade i=identidadeUsuario(original);
        if(!SessionSecurity.matches(session,token)) throw new ComplementoMobileException(403,"Entre novamente para continuar.");
        if(body==null||!body.isObject()||!body.has("nome")||!body.get("nome").isTextual()) throw invalida();
        var keys=body.fieldNames();while(keys.hasNext()) if(!Set.of("nome","telefone").contains(keys.next())) throw invalida();
        if(body.has("telefone")&&!body.get("telefone").isTextual()) throw invalida();
        Resumo saved=service.salvar(i.perfil(),i.id(),new Alteracao(body.get("nome").textValue(),body.has("telefone")?body.get("telefone").textValue():null,body.has("telefone")));
        // Só após o retorno transacional: atualiza a apresentação, sem renovar nonce ou autoridade.
        if(!i.equals(identidade(r))||!SessionSecurity.matches(session,token))
            throw new ComplementoMobileException(401,"Sua sessão mudou. Entre novamente para continuar.");
        // Muta somente a referência do solicitante capturada antes da transação.
        // Um login concorrente pode substituir o atributo da sessão durante a gravação.
        Object u=original;
        if(u instanceof Cliente c) { c.setNome(saved.nome());c.setTelefone(saved.telefone()); }
        else if(u instanceof Corretor c) { c.setNome(saved.nome());c.setTelefone(saved.telefone()); }
        else if(u instanceof Admin a) a.setName(saved.nome());
        return ok(saved);
    }
    private static Identidade identidade(HttpServletRequest r) {
        Object u=r.getSession(false)==null?null:r.getSession(false).getAttribute(SessionSecurity.USER_ATTRIBUTE);
        return identidadeUsuario(u);
    }
    private static Identidade identidadeUsuario(Object u) {
        Identidade i=u instanceof Cliente c?new Identidade("CLIENTE",c.getId()):u instanceof Corretor c?new Identidade("CORRETOR",c.getId()):u instanceof Admin a?new Identidade("ADMIN",a.getId()):null;
        if(i==null||i.id()==null||i.id()<=0) throw new ComplementoMobileException(401,"Entre novamente para continuar.");
        return i;
    }
    private record Identidade(String perfil,Integer id) { }
    private static ComplementoMobileException invalida() { return new ComplementoMobileException(400,"Altere somente os campos permitidos do seu perfil."); }
}
