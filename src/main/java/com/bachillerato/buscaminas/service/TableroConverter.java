package com.bachillerato.buscaminas.service;

import com.bachillerato.buscaminas.model.Tablero;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

// Convierte el Tablero (objeto Java) <-> String JSON que se guarda en Partida.tablero.
// Usa el ObjectMapper que Spring Boot ya configura e inyecta.
@Component
public class TableroConverter {

    private final ObjectMapper objectMapper;

    public TableroConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String aJson(Tablero tablero) {
        try {
            return objectMapper.writeValueAsString(tablero);
        } catch (JsonProcessingException e) {
            // Si pasa, es un error de programación, no del usuario: termina en 500.
            throw new IllegalStateException("No se pudo convertir el tablero a JSON", e);
        }
    }

    public Tablero desdeJson(String json) {
        try {
            return objectMapper.readValue(json, Tablero.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("El tablero guardado no es un JSON válido", e);
        }
    }
}
