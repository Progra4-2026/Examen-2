package com.bachillerato.buscaminas.controller;

import com.bachillerato.buscaminas.dto.PartidaDtos.NivelResponse;
import com.bachillerato.buscaminas.model.enums;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/niveles")
public class NivelController {

    @GetMapping
    public List<NivelResponse> listar() {
        return Arrays.stream(enums.Nivel.values())
                .map(NivelResponse::desde)
                .toList();
    }
}
