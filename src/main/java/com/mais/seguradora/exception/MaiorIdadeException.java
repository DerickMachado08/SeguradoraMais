package com.mais.seguradora.exception;

public class MaiorIdadeException extends RuntimeException {
    public MaiorIdadeException() {
        super("por favor informar sua data de nascimento correta");
    }
}
