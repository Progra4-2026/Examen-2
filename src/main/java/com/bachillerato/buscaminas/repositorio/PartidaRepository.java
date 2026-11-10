package com.bachillerato.buscaminas.repositorio;

import com.bachillerato.buscaminas.modelo.entidad.Partida;
import com.bachillerato.buscaminas.modelo.enums;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PartidaRepository extends JpaRepository<Partida, Long> {

    // Historial completo de un usuario.
    // Usa: idUsuario (dueño de las partidas).
    // Devuelve: todas sus partidas, ordenadas por fechaInicio de la más reciente a la más vieja.
    @Query("""
            SELECT p
            FROM Partida p
            WHERE p.usuario.idUsuario = :idUsuario
            ORDER BY p.fechaInicio DESC
            """)
    List<Partida> historialPartidas(Long idUsuario);

    // Historial de un usuario en un solo nivel.
    // Usa: idUsuario (dueño) y nivel (PRINCIPIANTE, INTERMEDIO o EXPERTO).
    // Devuelve: sus partidas de ese nivel, ordenadas por fechaInicio de la más reciente a la más vieja.
    @Query("""
            SELECT p
            FROM Partida p
            WHERE p.usuario.idUsuario = :idUsuario
              AND p.nivel = :nivel    
            ORDER BY p.fechaInicio DESC
            """)
    List<Partida> historialPartidasPorNivel(Long idUsuario, enums.Nivel nivel);

    // Última partida de un usuario en un estado dado (para continuar la que está en curso).
    // Usa: idUsuario (dueño), estado (normalmente EN_CURSO) y limit (llamar con Limit.of(1)).
    // Devuelve: la partida con la fechaInicio más reciente en ese estado, o vacío si no tiene ninguna.
    @Query("""
            SELECT p
            FROM Partida p
            WHERE p.usuario.idUsuario = :idUsuario
              AND p.estado = :estado
            ORDER BY p.fechaInicio DESC
            """)
    Optional<Partida> ultimaPartidaPorEstado(Long idUsuario, enums.EstadoPartida estado, Limit limit);

    // Carga una partida solo si pertenece al usuario (evita que alguien abra partidas ajenas).
    // Usa: idPartida (la partida buscada) e idUsuario (quien la pide).
    // Devuelve: la partida si existe y es de ese usuario; si no, vacío.
    @Query("""
            SELECT p
            FROM Partida p
            WHERE p.idPartida = :idPartida
              AND p.usuario.idUsuario = :idUsuario
            """)
    Optional<Partida> buscarPartidaDelUsuario(Long idPartida, Long idUsuario);

    // Estadística: cantidad de partidas de un usuario en un estado.
    // Usa: idUsuario (dueño) y estado (EN_CURSO, TERMINADA o ABANDONADA).
    // Devuelve: el número de partidas que cumplen ambos filtros.
    @Query("""
            SELECT COUNT(p)
            FROM Partida p
            WHERE p.usuario.idUsuario = :idUsuario
              AND p.estado = :estado
            """)
    long numPartidasPorEstado(Long idUsuario, enums.EstadoPartida estado);

    // Ranking de mejores tiempos de un nivel (de todos los usuarios).
    // Usa: nivel, estado (normalmente TERMINADA) y limit (llamar con Limit.of(10) para el top 10).
    // Devuelve: las partidas ordenadas por tiempoMs de menor a mayor (el más rápido primero).
    @Query("""
            SELECT p
            FROM Partida p
            WHERE p.nivel = :nivel
              AND p.estado = :estado
            ORDER BY p.tiempoMs ASC
            """)
    List<Partida> top10PartidasPorTiempoMs(enums.Nivel nivel, enums.EstadoPartida estado, Limit limit);
}
