package com.bachillerato.buscaminas.service;

import com.bachillerato.buscaminas.dto.PartidaDtos.PartidaResponse;
import com.bachillerato.buscaminas.excepcion.ApiException;
import com.bachillerato.buscaminas.model.Tablero;
import com.bachillerato.buscaminas.model.entity.Partida;
import com.bachillerato.buscaminas.model.entity.Usuario;
import com.bachillerato.buscaminas.model.enums;
import com.bachillerato.buscaminas.repository.PartidaRepository;
import com.bachillerato.buscaminas.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class PartidaService {

    private final PartidaRepository partidaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TableroConverter tableroConverter;

    // SecureRandom: la posición de las minas no se puede predecir adivinando la semilla.
    private final Random random = new SecureRandom();

    public PartidaService(PartidaRepository partidaRepository,
                          UsuarioRepository usuarioRepository,
                          TableroConverter tableroConverter) {
        this.partidaRepository = partidaRepository;
        this.usuarioRepository = usuarioRepository;
        this.tableroConverter = tableroConverter;
    }

    // POST /api/partidas
    // Todo ocurre en UNA transacción: el bloqueo del usuario dura hasta que la partida se guarda.
    @Transactional
    public PartidaResponse crearPartida(String nombreUsuario, enums.Nivel nivel) {
        // 1. Bloquear la fila del usuario (SELECT ... FOR UPDATE).
        Usuario usuario = usuarioRepository.bloquearPorNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "La sesión no corresponde a un usuario válido."));

        // 2. Regla: un usuario no puede tener dos partidas EN_CURSO.
        //    Gracias al bloqueo, una petición simultánea espera aquí hasta que esta termine,
        //    y para entonces ya ve la partida creada.
        if (partidaRepository.numPartidasPorEstado(usuario.getIdUsuario(),
                enums.EstadoPartida.EN_CURSO) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya tienes una partida en curso.");
        }

        // 3. Crear el tablero con las minas al azar.
        Tablero tablero = new Tablero(nivel.getFilas(), nivel.getColumnas(), nivel.getMinas());
        colocarMinas(tablero);

        // 4. Guardar la partida. @PrePersist pone estado EN_CURSO y fechaInicio.
        Partida partida = new Partida();
        partida.setUsuario(usuario);
        partida.setNivel(nivel);
        partida.setTablero(tableroConverter.aJson(tablero));
        partida = partidaRepository.save(partida);

        // 5. Responder con el tablero FILTRADO (sin minas).
        return PartidaResponse.desde(partida, tablero);
    }

    // Coloca las minas sin repetir posiciones:
    // numera todas las casillas (0 .. filas*columnas-1), las baraja y toma las primeras N.
    private void colocarMinas(Tablero tablero) {
        int total = tablero.getFilas() * tablero.getColumnas();
        List<Integer> posiciones = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            posiciones.add(i);
        }
        Collections.shuffle(posiciones, random);

        for (int i = 0; i < tablero.getNumMinas(); i++) {
            int posicion = posiciones.get(i);
            int fila = posicion / tablero.getColumnas();
            int columna = posicion % tablero.getColumnas();
            tablero.agregarMina(fila, columna);
        }
    }
}
