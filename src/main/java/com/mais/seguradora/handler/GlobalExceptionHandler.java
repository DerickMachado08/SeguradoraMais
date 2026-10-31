package com.mais.seguradora.handler;

import com.mais.seguradora.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                erros.put(error.getField(),
                        error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleRecursoDuplicado(
            RecursoDuplicadoException e) {

        Map<String, String> erro = new HashMap<>();

        if (e.getMessage().toLowerCase().contains("cpf")) {
            erro.put("cpf", e.getMessage());
        } else if (e.getMessage().toLowerCase().contains("email")) {
            erro.put("email", e.getMessage());
        }

        return ResponseEntity
                .badRequest()
                .body(erro);
    }

    @ExceptionHandler(MenorIdadeException.class)
    public ResponseEntity<Map<String, String>> handlermenoridade(MenorIdadeException e) {
        Map<String, String> erro = new HashMap<>();
        erro.put("dataNascimento", e.getMessage());

        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(MaiorIdadeException.class)
    public ResponseEntity<Map<String, String>> handlermaioridade(MaiorIdadeException e) {
        Map<String, String> erro = new HashMap<>();
        erro.put("dataNascimento", e.getMessage());

        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(RecursoNullException.class)
    public ResponseEntity<Map<String, String>> handleRecursoNull(RecursoNullException e) {

        Map<String, String> erro = new HashMap<>();

        if (e.getMessage().toLowerCase().contains("email")) {
            erro.put("email", e.getMessage());
        } else if (e.getMessage().toLowerCase().contains("senha")) {
            erro.put("senha", e.getMessage());
        }

            return ResponseEntity
                    .badRequest()
                    .body(erro);
        }

    }

