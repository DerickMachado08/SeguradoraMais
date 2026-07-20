package com.mais.seguradora.exception;

public class ContratoPendenteNullException extends RuntimeException {
    public ContratoPendenteNullException() {
        super("Somente contratacoes pendentes podem ser emitidas");
    }
}
