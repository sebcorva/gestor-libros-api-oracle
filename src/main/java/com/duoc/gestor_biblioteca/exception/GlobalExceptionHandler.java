package com.duoc.gestor_biblioteca.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LibroNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNoEncontrado(LibroNoEncontradoException ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("message", ex.getMessage());
        return respuesta(HttpStatus.NOT_FOUND, cuerpo);
    }

    // @Valid fallo: un mensaje por campo, para mostrarlo junto a cada input del formulario
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("errors", errores);
        return respuesta(HttpStatus.BAD_REQUEST, cuerpo);
    }

    // JSON mal formado, body vacio o un tipo incorrecto (por ejemplo "anioPublicacion": "abc")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBodyIlegible(HttpMessageNotReadableException ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("message", "El cuerpo de la peticion no es un JSON valido");
        return respuesta(HttpStatus.BAD_REQUEST, cuerpo);
    }

    // Un parametro de la URL con el tipo incorrecto (por ejemplo /libros/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("message", "El parametro '" + ex.getName() + "' tiene un valor invalido: " + ex.getValue());
        return respuesta(HttpStatus.BAD_REQUEST, cuerpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        Map<String, Object> cuerpo = new HashMap<>();

        if (ex instanceof ErrorResponse errorResponse) {
            cuerpo.put("message", errorResponse.getBody().getDetail());
            return respuesta(errorResponse.getStatusCode(), cuerpo);
        }

        log.error("Error inesperado", ex);
        cuerpo.put("message", "Ocurrio un error inesperado");
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, cuerpo);
    }

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatusCode status, Map<String, Object> cuerpo) {
        cuerpo.put("status", status.value());
        cuerpo.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
