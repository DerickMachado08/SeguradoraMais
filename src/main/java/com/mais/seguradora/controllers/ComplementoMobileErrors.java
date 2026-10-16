package com.mais.seguradora.controllers;

import com.mais.seguradora.dto.ClienteAreaDtos.Erro;
import com.mais.seguradora.exception.ComplementoMobileException;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Handlers herdados somente pelos dois novos controllers; preserva contratos legados. */
public abstract class ComplementoMobileErrors {
    protected static ResponseEntity<?> ok(Object body) { return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body); }
    @ExceptionHandler(ComplementoMobileException.class) public ResponseEntity<?> erro(ComplementoMobileException e) { return ResponseEntity.status(e.status()).cacheControl(CacheControl.noStore()).body(new Erro("OPERACAO_NAO_CONFIRMADA",e.getMessage(),Map.of())); }
    @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> invalido() { return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(new Erro("DADOS_INVALIDOS","Confira os dados informados.",Map.of())); }
    @ExceptionHandler(Exception.class) public ResponseEntity<?> indisponivel() { return ResponseEntity.status(503).cacheControl(CacheControl.noStore()).body(new Erro("INDISPONIVEL","Operação não confirmada. Atualize os dados antes de tentar novamente.",Map.of())); }
}
