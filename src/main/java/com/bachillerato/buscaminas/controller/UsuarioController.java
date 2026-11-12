package com.bachillerato.buscaminas.controller;

import com.bachillerato.buscaminas.dto.PartidaDtos.HistorialItemResponse;
import com.bachillerato.buscaminas.model.enums;
import com.bachillerato.buscaminas.service.PartidaService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final PartidaService partidaService;

    public UsuarioController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    // Historial de partidas del usuario con fecha, hora, nivel y resultado.
    // Filtro opcional por nivel: /api/usuarios/2/partidas?nivel=PRINCIPIANTE
    // 200 con el historial; 403 si se consultan las partidas de otro usuario.
    @GetMapping("/{id}/partidas")
    public List<HistorialItemResponse> historial(@PathVariable Long id,
                                                 @RequestParam(required = false) enums.Nivel nivel,
                                                 Authentication authentication) {
        return partidaService.obtenerHistorial(authentication.getName(), id, nivel);
    }
}
