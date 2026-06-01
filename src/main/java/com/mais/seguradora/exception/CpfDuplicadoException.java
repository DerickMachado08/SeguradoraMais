package com.mais.seguradora.exception;

public class CpfDuplicadoException extends RuntimeException {
    public CpfDuplicadoException(){
        super("Cpf ja cadastrado!");
    }
}
