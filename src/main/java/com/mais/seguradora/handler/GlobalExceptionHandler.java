package com.mais.seguradora.handler;

import com.mais.seguradora.exception.CpfDuplicadoException;
import com.mais.seguradora.exception.EmailDuplicadoException;
import com.mais.seguradora.exception.MenorIdadeException;
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

    @ExceptionHandler(CpfDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handlercpf(CpfDuplicadoException e){
                Map<String, String> erro = new HashMap<>();
                erro.put("cpf", e.getMessage());

                return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handlercpf(EmailDuplicadoException e){
        Map<String, String> erro = new HashMap<>();
        erro.put("email", e.getMessage());

        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(MenorIdadeException.class)
    public ResponseEntity<Map<String, String>> handlercpf(MenorIdadeException e){
        Map<String, String> erro = new HashMap<>();
        erro.put("dataNascimento", e.getMessage());

        return ResponseEntity.badRequest().body(erro);
    }


}
