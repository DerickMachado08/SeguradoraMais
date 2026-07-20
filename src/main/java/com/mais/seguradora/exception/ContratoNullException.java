package com.mais.seguradora.exception;

public class ContratoNullException extends RuntimeException {
    public ContratoNullException() {
        super("Contratacao nao encontrada");
    }
}
