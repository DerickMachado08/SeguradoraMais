package com.mais.seguradora.exception;

public final class ComplementoMobileException extends RuntimeException {
    private final int status;
    public ComplementoMobileException(int status, String message) { super(message); this.status = status; }
    public int status() { return status; }
}
