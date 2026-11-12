package com.bachillerato.buscaminas.excepcion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
                .map(error -> error.getDefaultMessage())
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

    // Un parámetro de la URL con un tipo incorrecto: /api/partidas/abc o ?nivel=FACIL
    @ExceptionHandler(MethodArgumentTypeMismatchException.class) // → 400
    public ResponseEntity<Map<String, String>> manejarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        log.warn("Parámetro inválido '{}': {}", ex.getName(), ex.getValue());

        return ResponseEntity.badRequest()
                .body(Map.of("error", "El parámetro '" + ex.getName() + "' no es válido."));
    }

    // Ruta que no existe: /api/loquesea
    @ExceptionHandler(NoResourceFoundException.class) // → 404
    public ResponseEntity<Map<String, String>> manejarRutaInexistente(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "El recurso solicitado no existe."));
    }

    // Ruta que existe pero con otro verbo: DELETE /api/ranking
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class) // → 405
    public ResponseEntity<Map<String, String>> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "El método " + ex.getMethod() + " no está permitido en esta ruta."));
    }

    // Cuerpo enviado sin Content-Type: application/json
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class) // → 415
    public ResponseEntity<Map<String, String>> manejarTipoNoSoportado(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(Map.of("error", "El cuerpo de la petición debe enviarse como application/json."));
    }

    // Dos acciones simultáneas sobre la misma partida: @Version detecta que la fila cambió
    // entre la lectura y el guardado, y la segunda se rechaza en vez de pisar a la primera.
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class) // → 409
    public ResponseEntity<Map<String, String>> manejarConcurrencia(ObjectOptimisticLockingFailureException ex) {
        log.warn("Conflicto de versión: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "La partida cambió mientras se procesaba la acción. Intenta de nuevo."));
    }

    @ExceptionHandler(Exception.class) // → 500
    public ResponseEntity<Map<String, String>> manejarGeneral(Exception ex) {
        log.error("Error no controlado", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor."));
    }
}