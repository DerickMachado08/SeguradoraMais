package com.mais.seguradora.exception;

public class MenorIdadeException extends RuntimeException {
    public MenorIdadeException() {
        super("Menor de idade não permitido");
    }
}
