package com.bachillerato.buscaminas.controller;

import com.bachillerato.buscaminas.dto.PartidaDtos.CrearPartidaRequest;
import com.bachillerato.buscaminas.dto.PartidaDtos.PartidaResponse;
import com.bachillerato.buscaminas.service.PartidaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partidas")
public class PartidaController {

    private final PartidaService partidaService;

    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    // Crea una partida nueva para el usuario de la sesión.
    // 201 con el tablero filtrado; 401 sin sesión (lo maneja Spring Security); 409 si ya tiene una en curso.
    @PostMapping
    public ResponseEntity<PartidaResponse> crear(@Valid @RequestBody CrearPartidaRequest request,
                                                 Authentication authentication) {
        PartidaResponse partida = partidaService.crearPartida(authentication.getName(), request.nivel());
        return ResponseEntity.status(HttpStatus.CREATED).body(partida);
    }

    // Devuelve la partida en curso del usuario de la sesión.
    // 200 con el tablero filtrado; 404 si no tiene ninguna en curso.
    @GetMapping("/actual")
    public PartidaResponse actual(Authentication authentication) {
        return partidaService.obtenerPartidaActual(authentication.getName());
    }
}
