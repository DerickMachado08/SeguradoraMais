package com.mais.seguradora.dto;

public record SessaoResponse(String perfil, UsuarioResumo usuario, String csrfToken) {
    public record UsuarioResumo(Integer id, String nome, String email) { }
    @Override public String toString() { return "SessaoResponse[conteúdo omitido]"; }
}
