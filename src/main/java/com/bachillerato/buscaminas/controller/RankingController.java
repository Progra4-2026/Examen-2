package com.bachillerato.buscaminas.controller;

import com.bachillerato.buscaminas.dto.RankingDtos.RankingNivelResponse;
import com.bachillerato.buscaminas.service.RankingService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    // Ranking de mejores tiempos por nivel, calculado desde el historial.
    // 200 con la tabla de posiciones de cada nivel y la posición del usuario actual.
    @GetMapping
    public List<RankingNivelResponse> ranking(Authentication authentication) {
        return rankingService.obtenerRanking(authentication.getName());
    }
}
