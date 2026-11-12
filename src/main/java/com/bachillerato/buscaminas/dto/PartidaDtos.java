package com.bachillerato.buscaminas.dto;

import com.bachillerato.buscaminas.model.enums;

public final class PartidaDtos {
    private PartidaDtos() {}

    public record NivelResponse(
            String nombre,
            int filas,
            int columnas,
            int minas
    ) {
        public static NivelResponse desde(enums.Nivel nivel) {
            return new NivelResponse(nivel.name(), nivel.getFilas(),
                    nivel.getColumnas(), nivel.getMinas());
        }
    }
}
