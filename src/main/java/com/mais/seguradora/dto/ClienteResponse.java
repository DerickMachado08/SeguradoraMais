package com.mais.seguradora.dto;

import com.mais.seguradora.model.Cliente;
import java.time.LocalDate;

/** Preserva os campos públicos da resposta usada pelo site, sem senha ou hash. */
public record ClienteResponse(Integer id, String nome, String cpf, LocalDate dataNascimento,
                              String email, String telefone, String logradouro, String estado,
                              String cidade, String bairro, String numero, String cep) {
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getCpf(), cliente.getDataNascimento(),
                cliente.getEmail(), cliente.getTelefone(), cliente.getLogradouro(), cliente.getEstado(),
                cliente.getCidade(), cliente.getBairro(), cliente.getNumero(), cliente.getCep());
    }
    @Override public String toString() { return "ClienteResponse[conteúdo omitido]"; }
}
