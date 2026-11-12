package com.bachillerato.buscaminas.model;

public class enums {

    // Los niveles son constantes del backend (no hay tabla de catálogo).
    public enum Nivel {
        PRINCIPIANTE(9, 9, 10),
        INTERMEDIO(16, 16, 40),
        EXPERTO(16, 30, 99);

        private final int filas;
        private final int columnas;
        private final int minas;

        Nivel(int filas, int columnas, int minas) {
            this.filas = filas;
            this.columnas = columnas;
            this.minas = minas;
        }

        public int getFilas() { return filas; }
        public int getColumnas() { return columnas; }
        public int getMinas() { return minas; }
    }

    public enum EstadoPartida { EN_CURSO, TERMINADA, ABANDONADA }
    public enum Rol {JUGADOR, ADMIN}
}
