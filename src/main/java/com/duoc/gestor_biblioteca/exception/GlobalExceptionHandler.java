package com.duoc.gestor_biblioteca.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNoEncontrado(LibroNoEncontradoException ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("message", ex.getMessage());
        return respuesta(HttpStatus.NOT_FOUND, cuerpo);
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, Map<String, Object> cuerpo) {
        cuerpo.put("status", status.value());
        cuerpo.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
