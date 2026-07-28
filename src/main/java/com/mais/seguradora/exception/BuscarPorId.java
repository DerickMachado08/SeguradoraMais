package com.mais.seguradora.exception;

public class BuscarPorId extends RuntimeException {
    public BuscarPorId() {
        super("id não encontrado");
    }
}
