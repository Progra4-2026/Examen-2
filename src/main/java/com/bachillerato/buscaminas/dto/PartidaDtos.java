package com.bachillerato.buscaminas.dto;

import com.bachillerato.buscaminas.model.Tablero;
import com.bachillerato.buscaminas.model.entity.Partida;
import com.bachillerato.buscaminas.model.enums;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

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

    // Body de POST /api/partidas: {"nivel": "PRINCIPIANTE"}
    // Si el nivel no es uno de los 3 válidos, Jackson falla al leerlo y el manejador global responde 400.
    public record CrearPartidaRequest(
            @NotNull(message = "El nivel es obligatorio")
            enums.Nivel nivel
    ) { }

    // Body de POST /api/partidas/{id}/acciones: {"tipo": "REVELAR", "fila": 3, "columna": 5}
    // Integer (y no int) para que @NotNull detecte cuando el campo no viene en el JSON.
    public record AccionRequest(
            @NotNull(message = "El tipo de acción es obligatorio")
            enums.TipoAccion tipo,

            @NotNull(message = "La fila es obligatoria")
            Integer fila,

            @NotNull(message = "La columna es obligatoria")
            Integer columna
    ) { }

    // Valores posibles de cada casilla en el tablero filtrado.
    public static final String OCULTA = "OCULTA";
    public static final String BANDERA = "BANDERA";
    public static final String MINA = "MINA";
    // Una casilla revelada se envía como su número de minas adyacentes: "0" a "8".

    // Una fila del historial del usuario. No incluye el tablero: el historial solo muestra
    // el resumen, y el detalle completo se pide con GET /api/partidas/{id}.
    public record HistorialItemResponse(
            Long id,
            String nivel,
            String estado,
            String resultado,
            Integer tiempoMs,
            int movimientos,
            int estrellas,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {
        public static HistorialItemResponse desde(Partida partida) {
            return new HistorialItemResponse(
                    partida.getIdPartida(),
                    partida.getNivel().name(),
                    partida.getEstado().name(),
                    resultadoDe(partida),
                    partida.getTiempoMs(),
                    partida.getMovimientos(),
                    partida.getEstrellas(),
                    partida.getFechaInicio(),
                    partida.getFechaFin());
        }
    }

    // Calcula el resultado de una partida (null mientras sigue EN_CURSO).
    // Regla del proyecto: al ganar se guarda tiempo_ms; al perder, tiempo_ms queda NULL.
    public static String resultadoDe(Partida partida) {
        return switch (partida.getEstado()) {
            case EN_CURSO -> null;
            case ABANDONADA -> enums.Resultado.ABANDONADA.name();
            case TERMINADA -> partida.getTiempoMs() != null
                    ? enums.Resultado.VICTORIA.name()
                    : enums.Resultado.DERROTA.name();
        };
    }

    // Estado de una partida tal como lo ve el jugador.
    // NUNCA incluye la lista de minas: solo lo que el jugador tiene derecho a ver.
    public record PartidaResponse(
            Long id,
            String nivel,
            String estado,
            String resultado,
            int filas,
            int columnas,
            int numMinas,
            int banderasColocadas,
            int movimientos,
            Integer tiempoMs,
            int estrellas,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String[][] casillas
    ) {
        public static PartidaResponse desde(Partida partida, Tablero tablero) {
            boolean enCurso = partida.getEstado() == enums.EstadoPartida.EN_CURSO;
            String[][] casillas = new String[tablero.getFilas()][tablero.getColumnas()];

            for (int f = 0; f < tablero.getFilas(); f++) {
                for (int c = 0; c < tablero.getColumnas(); c++) {
                    casillas[f][c] = valorVisible(tablero, f, c, enCurso);
                }
            }

            return new PartidaResponse(
                    partida.getIdPartida(),
                    partida.getNivel().name(),
                    partida.getEstado().name(),
                    resultadoDe(partida),
                    tablero.getFilas(),
                    tablero.getColumnas(),
                    tablero.getNumMinas(),
                    tablero.getBanderas().size(),
                    partida.getMovimientos(),
                    partida.getTiempoMs(),
                    partida.getEstrellas(),
                    partida.getFechaInicio(),
                    partida.getFechaFin(),
                    casillas);
        }

        // Aquí está el filtro: decide qué puede ver el jugador de cada casilla.
        private static String valorVisible(Tablero tablero, int f, int c, boolean enCurso) {
            if (tablero.estaRevelada(f, c)) {
                // Una mina revelada solo existe cuando el jugador perdió (partida ya terminada).
                return tablero.esMina(f, c) ? MINA : String.valueOf(tablero.minasAdyacentes(f, c));
            }
            // Con la partida terminada ya se pueden mostrar todas las minas.
            if (!enCurso && tablero.esMina(f, c)) {
                return MINA;
            }
            if (tablero.tieneBandera(f, c)) {
                return BANDERA;
            }
            return OCULTA;
        }
    }
}
