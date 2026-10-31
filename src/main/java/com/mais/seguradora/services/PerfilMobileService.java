package com.mais.seguradora.services;

import com.mais.seguradora.Repositories.*;
import com.mais.seguradora.dto.PerfilMobileDtos.*;
import com.mais.seguradora.exception.ComplementoMobileException;
import com.mais.seguradora.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class PerfilMobileService {
    private final ClienteRepository clientes;private final CorretorRepository corretores;private final AdminRepository admins;
    public PerfilMobileService(ClienteRepository c,CorretorRepository r,AdminRepository a) { clientes=c;corretores=r;admins=a; }
    public Resumo consultar(String perfil,Integer id) { return projetar(buscar(perfil,id,false)); }
    @Transactional
    public Resumo salvar(String perfil,Integer id,Alteracao pedido) {
        Object usuario=buscar(perfil,id,true);
        int limite="CLIENTE".equals(perfil)?80:100;
        String nome=pedido.nome()==null?"":pedido.nome().strip().replaceAll("\\s+"," ");
        if(nome.length()<3||nome.length()>limite||!nome.matches("[\\p{L} ]+")
                ||("CLIENTE".equals(perfil)&&!nome.matches("[A-Za-zÀ-ÿ ]+")))
            throw new ComplementoMobileException(400,"Informe um nome válido, com 3 a "+limite+" caracteres, compatível com seu cadastro.");
        if(usuario instanceof Admin) {
            if(pedido.telefoneInformado()) throw new ComplementoMobileException(400,"Somente o nome pode ser alterado neste perfil.");
            ((Admin)usuario).setName(nome);admins.save((Admin)usuario);
        } else {
            String telefone=pedido.telefone()==null?"":pedido.telefone().replaceAll("[ ()+.-]","");
            if(!pedido.telefoneInformado()||!telefone.matches("[0-9]{10,13}")) throw new ComplementoMobileException(400,"Informe um telefone com 10 a 13 dígitos.");
            if(usuario instanceof Cliente c) { c.setNome(nome);c.setTelefone(telefone);clientes.save(c); }
            else { Corretor c=(Corretor)usuario;c.setNome(nome);c.setTelefone(telefone);corretores.save(c); }
        }
        return projetar(usuario);
    }
    private Object buscar(String perfil,Integer id,boolean lock) {
        if(id==null||id<=0) throw sessao();
        return switch(perfil) {
            case "CLIENTE" -> (lock?clientes.findForSolicitacao(id):clientes.findById(id)).orElseThrow(PerfilMobileService::sessao);
            case "CORRETOR" -> (lock?corretores.findForAdminOperation(id):corretores.findById(id)).orElseThrow(PerfilMobileService::sessao);
            case "ADMIN" -> (lock?admins.findForPerfil(id):admins.findById(id)).orElseThrow(PerfilMobileService::sessao);
            default -> throw sessao();
        };
    }
    private static Resumo projetar(Object u) {
        if(u instanceof Cliente c) return new Resumo(c.getId(),"CLIENTE",c.getNome(),c.getEmail(),c.getTelefone(),true,80);
        if(u instanceof Corretor c) return new Resumo(c.getId(),"CORRETOR",c.getNome(),c.getEmail(),c.getTelefone(),true,100);
        Admin a=(Admin)u;return new Resumo(a.getId(),"ADMIN",a.getName(),a.getEmail(),null,false,100);
    }
    private static ComplementoMobileException sessao() { return new ComplementoMobileException(401,"Entre novamente para continuar."); }
}
