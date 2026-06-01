package com.mais.seguradora.exception;

public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException() {
        super("Email ja cadastrado!");
    }
}
