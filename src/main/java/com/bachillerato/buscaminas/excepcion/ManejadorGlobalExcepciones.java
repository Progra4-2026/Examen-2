package com.bachillerato.buscaminas.excepcion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> manejarApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // → 400
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Datos inválidos.");

        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class) // → 400
    public ResponseEntity<Map<String, String>> manejarCuerpoInvalido(HttpMessageNotReadableException ex) {
        log.warn("Cuerpo de petición inválido: {}", ex.getMostSpecificCause().getMessage());

        return ResponseEntity.badRequest()
                .body(Map.of("error", "El cuerpo de la petición no es válido."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class) // → 409
    public ResponseEntity<Map<String, String>> manejarConflictoDatos(DataIntegrityViolationException ex) {
        log.warn("Los datos entran en conflicto con un registro existente: {}", ex.getMostSpecificCause().getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Los datos entran en conflicto con un registro existente."));
    }

    @ExceptionHandler(Exception.class) // → 500
    public ResponseEntity<Map<String, String>> manejarGeneral(Exception ex) {
        log.error("Error no controlado", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor."));
    }
}