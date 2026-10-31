package com.mais.seguradora.dto;

public final class PerfilMobileDtos {
    private PerfilMobileDtos() { }
    public record Resumo(Integer id,String perfil,String nome,String email,String telefone,boolean editaTelefone,int limiteNome) { }
    public record Alteracao(String nome,String telefone,boolean telefoneInformado) { }
}
