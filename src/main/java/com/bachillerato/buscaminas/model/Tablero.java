package com.bachillerato.buscaminas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

// Estado completo del tablero, tal como se guarda en la columna JSON «tablero» de la tabla partida.
// Mismo formato que la partida de ejemplo de minas.sql:
// {"filas":9,"columnas":9,"num_minas":10,"minas":[[0,5],...],"reveladas":[[0,0],...],"banderas":[[0,5],...]}
// Cada casilla es un arreglo [fila, columna].
// OJO: esta clase incluye las minas, así que NUNCA se envía al frontend; para eso existe el DTO filtrado.
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tablero {

    private int filas;
    private int columnas;

    @JsonProperty("num_minas")
    private int numMinas;

    private List<int[]> minas = new ArrayList<>();
    private List<int[]> reveladas = new ArrayList<>();
    private List<int[]> banderas = new ArrayList<>();

    // Jackson necesita un constructor vacío para leer el JSON.
    public Tablero() { }

    public Tablero(int filas, int columnas, int numMinas) {
        this.filas = filas;
        this.columnas = columnas;
        this.numMinas = numMinas;
    }

    // ---------- Consultas sobre una casilla ----------

    public boolean dentroDelTablero(int fila, int columna) {
        return fila >= 0 && fila < filas && columna >= 0 && columna < columnas;
    }

    public boolean esMina(int fila, int columna) {
        return contiene(minas, fila, columna);
    }

    public boolean estaRevelada(int fila, int columna) {
        return contiene(reveladas, fila, columna);
    }

    public boolean tieneBandera(int fila, int columna) {
        return contiene(banderas, fila, columna);
    }

    // Cuenta las minas en las 8 casillas vecinas (0 a 8).
    public int minasAdyacentes(int fila, int columna) {
        int total = 0;
        for (int df = -1; df <= 1; df++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (df == 0 && dc == 0) continue;          // la casilla misma no cuenta
                int f = fila + df;
                int c = columna + dc;
                if (dentroDelTablero(f, c) && esMina(f, c)) total++;
            }
        }
        return total;
    }

    // ---------- Modificaciones ----------

    public void agregarMina(int fila, int columna) {
        if (!esMina(fila, columna)) minas.add(new int[]{fila, columna});
    }

    public void revelar(int fila, int columna) {
        if (!estaRevelada(fila, columna)) reveladas.add(new int[]{fila, columna});
    }

    public void ponerBandera(int fila, int columna) {
        if (!tieneBandera(fila, columna)) banderas.add(new int[]{fila, columna});
    }

    public void quitarBandera(int fila, int columna) {
        banderas.removeIf(casilla -> casilla[0] == fila && casilla[1] == columna);
    }

    // Victoria: todas las casillas sin mina ya fueron reveladas.
    public boolean todasLasSegurasReveladas() {
        return reveladas.size() == filas * columnas - numMinas;
    }

    private static boolean contiene(List<int[]> lista, int fila, int columna) {
        for (int[] casilla : lista) {
            if (casilla[0] == fila && casilla[1] == columna) return true;
        }
        return false;
    }

    // ---------- Getters y setters (los usa Jackson) ----------

    public int getFilas() { return filas; }
    public void setFilas(int filas) { this.filas = filas; }

    public int getColumnas() { return columnas; }
    public void setColumnas(int columnas) { this.columnas = columnas; }

    public int getNumMinas() { return numMinas; }
    public void setNumMinas(int numMinas) { this.numMinas = numMinas; }

    public List<int[]> getMinas() { return minas; }
    public void setMinas(List<int[]> minas) { this.minas = minas; }

    public List<int[]> getReveladas() { return reveladas; }
    public void setReveladas(List<int[]> reveladas) { this.reveladas = reveladas; }

    public List<int[]> getBanderas() { return banderas; }
    public void setBanderas(List<int[]> banderas) { this.banderas = banderas; }
}
