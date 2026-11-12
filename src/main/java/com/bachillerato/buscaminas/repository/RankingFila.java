package com.bachillerato.buscaminas.repository;

// Projection (interfaz) para leer el resultado de la consulta nativa del ranking.
//
// El ranking no es una tabla sino un cálculo (MIN + GROUP BY), así que no puede ser una
// entidad @Entity: no tiene llave primaria propia. Spring Data crea un objeto que implementa
// esta interfaz y conecta cada getter con la columna del SELECT que tiene el mismo alias
// (getNombreUsuario() <- "AS nombreUsuario").
public interface RankingFila {
    Long getUsuarioId();
    String getNombreUsuario();
    String getNivel();
    Long getMejorTiempoMs();
    Long getPartidasGanadas();
}
