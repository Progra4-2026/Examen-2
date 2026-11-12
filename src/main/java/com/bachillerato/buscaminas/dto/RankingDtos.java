package com.bachillerato.buscaminas.dto;

import java.util.List;

public final class RankingDtos {
    private RankingDtos() {}

    // Una posición de la tabla. Sin email ni datos personales: solo lo que se muestra públicamente.
    public record PosicionResponse(
            int posicion,
            String nombreUsuario,
            long mejorTiempoMs,
            long partidasGanadas,
            boolean esUsuarioActual
    ) { }

    // Ranking de un nivel: los mejores (top) y la posición del usuario actual,
    // que puede estar fuera del top, o ser null si todavía no ha ganado en ese nivel.
    public record RankingNivelResponse(
            String nivel,
            List<PosicionResponse> top,
            PosicionResponse miPosicion,
            int totalJugadores
    ) { }
}
