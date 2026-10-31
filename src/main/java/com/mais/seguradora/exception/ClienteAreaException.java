package com.mais.seguradora.exception;

/** Erros públicos deliberados, sem mensagens de infraestrutura ou dados pessoais. */
public final class ClienteAreaException extends RuntimeException {
    private final int status;
    private final String codigo;
    public ClienteAreaException(int status, String codigo, String mensagem) {
        super(mensagem); this.status = status; this.codigo = codigo;
    }
    public int status() { return status; }
    public String codigo() { return codigo; }
}
