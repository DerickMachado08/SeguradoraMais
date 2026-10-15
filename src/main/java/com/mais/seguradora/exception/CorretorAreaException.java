package com.mais.seguradora.exception;

public final class CorretorAreaException extends RuntimeException {
    private final int status;
    private final String codigo;
    public CorretorAreaException(int status, String codigo, String mensagem) {
        super(mensagem); this.status = status; this.codigo = codigo;
    }
    public int status() { return status; }
    public String codigo() { return codigo; }
}
