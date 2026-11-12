package com.bachillerato.buscaminas.service;

import com.bachillerato.buscaminas.dto.PartidaDtos.AccionRequest;
import com.bachillerato.buscaminas.dto.PartidaDtos.PartidaResponse;
import com.bachillerato.buscaminas.model.Tablero;
import com.bachillerato.buscaminas.model.entity.Partida;
import com.bachillerato.buscaminas.model.entity.Usuario;
import com.bachillerato.buscaminas.model.enums;
import com.bachillerato.buscaminas.repository.PartidaRepository;
import com.bachillerato.buscaminas.repository.UsuarioRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Pruebas de la capa de servicio: apertura en cadena (flood fill) de PartidaService.
//
// No levantan Spring ni usan MySQL: los repositorios se reemplazan por mocks de Mockito
// y los tableros se arman a mano, con las minas en posiciones conocidas.
// Así cada prueba sabe exactamente qué casillas deberían abrirse.
class PartidaServiceTest {

    private PartidaRepository partidaRepository;
    private UsuarioRepository usuarioRepository;
    private TableroConverter tableroConverter;
    private PartidaService partidaService;

    @BeforeEach
    void setUp() {
        partidaRepository = mock(PartidaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        tableroConverter = new TableroConverter(new ObjectMapper());
        partidaService = new PartidaService(partidaRepository, usuarioRepository, tableroConverter);
    }

    @Test
    @DisplayName("Una casilla con 0 minas alrededor abre en cadena toda la zona vacía")
    void casillaVaciaAbreTodaLaZona() {
        // 5x5 con una sola mina en la esquina inferior derecha:
        //   0 0 0 0 0
        //   0 0 0 0 0
        //   0 0 0 0 0
        //   0 0 0 1 1
        //   0 0 0 1 *
        Tablero tablero = new Tablero(5, 5, 1);
        tablero.agregarMina(4, 4);

        partidaService.abrirEnCadena(tablero, 0, 0);

        // Un solo clic abre las 24 casillas seguras; la mina sigue oculta.
        assertEquals(24, tablero.getReveladas().size());
        assertFalse(tablero.estaRevelada(4, 4));
        assertTrue(tablero.todasLasSegurasReveladas());
    }

    @Test
    @DisplayName("La cadena se detiene en las casillas con número")
    void cadenaSeDetieneEnLosNumeros() {
        // 5x5 con una "pared" de minas en la columna 2:
        //   0 2 * 2 0
        //   0 3 * 3 0
        //   0 3 * 3 0
        //   0 3 * 3 0
        //   0 2 * 2 0
        Tablero tablero = new Tablero(5, 5, 5);
        for (int fila = 0; fila < 5; fila++) {
            tablero.agregarMina(fila, 2);
        }

        partidaService.abrirEnCadena(tablero, 0, 0);

        // Se abren las columnas 0 (vacías) y 1 (números, el borde de la cadena)...
        assertEquals(10, tablero.getReveladas().size());
        for (int fila = 0; fila < 5; fila++) {
            assertTrue(tablero.estaRevelada(fila, 0));
            assertTrue(tablero.estaRevelada(fila, 1));
            // ...pero la cadena no cruza la pared: ni minas ni el lado derecho.
            assertFalse(tablero.estaRevelada(fila, 2));
            assertFalse(tablero.estaRevelada(fila, 3));
            assertFalse(tablero.estaRevelada(fila, 4));
        }
    }

    @Test
    @DisplayName("Una casilla con número se abre sola, sin cadena")
    void casillaConNumeroNoPropaga() {
        Tablero tablero = new Tablero(5, 5, 1);
        tablero.agregarMina(2, 2);

        partidaService.abrirEnCadena(tablero, 1, 1);   // vecina de la mina -> número 1

        assertEquals(1, tablero.getReveladas().size());
        assertTrue(tablero.estaRevelada(1, 1));
    }

    @Test
    @DisplayName("Las casillas con bandera no se abren durante la cadena")
    void banderasNoSeAbrenEnLaCadena() {
        Tablero tablero = new Tablero(5, 5, 1);
        tablero.agregarMina(4, 4);
        tablero.ponerBandera(0, 4);                   // bandera en una casilla segura

        partidaService.abrirEnCadena(tablero, 0, 0);

        assertFalse(tablero.estaRevelada(0, 4));
        assertTrue(tablero.tieneBandera(0, 4));
        assertEquals(23, tablero.getReveladas().size());   // 24 seguras - 1 con bandera
    }

    @Test
    @DisplayName("procesarAccion: un clic en zona vacía abre en cadena y gana la partida")
    void procesarAccionAbreEnCadenaYGana() {
        // Usuario dueño de la partida
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombreUsuario("jugador1");

        // Partida en curso con el mismo tablero 5x5 de una mina
        Tablero tablero = new Tablero(5, 5, 1);
        tablero.agregarMina(4, 4);

        Partida partida = new Partida();
        partida.setIdPartida(10L);
        partida.setUsuario(usuario);
        partida.setNivel(enums.Nivel.PRINCIPIANTE);
        partida.setEstado(enums.EstadoPartida.EN_CURSO);
        partida.setFechaInicio(LocalDateTime.now().minusSeconds(30));
        partida.setTablero(tableroConverter.aJson(tablero));

        when(usuarioRepository.buscarPorNombreUsuario("jugador1")).thenReturn(Optional.of(usuario));
        when(partidaRepository.findById(10L)).thenReturn(Optional.of(partida));
        when(partidaRepository.save(any(Partida.class))).thenAnswer(inv -> inv.getArgument(0));

        PartidaResponse respuesta = partidaService.procesarAccion("jugador1", 10L,
                new AccionRequest(enums.TipoAccion.REVELAR, 0, 0));

        // La cadena abrió todo lo seguro -> victoria con tiempo medido en el servidor
        assertEquals("TERMINADA", respuesta.estado());
        assertNotNull(respuesta.tiempoMs());
        assertTrue(respuesta.estrellas() >= 1);
        assertEquals(1, respuesta.movimientos());
        assertEquals("0", respuesta.casillas()[0][0]);
        assertEquals("1", respuesta.casillas()[3][3]);
        assertEquals("MINA", respuesta.casillas()[4][4]);   // al terminar ya se muestran las minas
    }
}
