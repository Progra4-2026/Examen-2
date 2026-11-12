package com.bachillerato.buscaminas.service;

import com.bachillerato.buscaminas.dto.RankingDtos.PosicionResponse;
import com.bachillerato.buscaminas.dto.RankingDtos.RankingNivelResponse;
import com.bachillerato.buscaminas.excepcion.ApiException;
import com.bachillerato.buscaminas.model.entity.Usuario;
import com.bachillerato.buscaminas.model.enums;
import com.bachillerato.buscaminas.repository.PartidaRepository;
import com.bachillerato.buscaminas.repository.RankingFila;
import com.bachillerato.buscaminas.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {

    private static final int TAMANO_TOP = 10;

    private final PartidaRepository partidaRepository;
    private final UsuarioRepository usuarioRepository;

    public RankingService(PartidaRepository partidaRepository, UsuarioRepository usuarioRepository) {
        this.partidaRepository = partidaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // GET /api/ranking
    // Para cada nivel: el top 10 de mejores tiempos y la posición del usuario de la sesión.
    @Transactional(readOnly = true)
    public List<RankingNivelResponse> obtenerRanking(String nombreUsuario) {
        Usuario usuario = usuarioRepository.buscarPorNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "La sesión no corresponde a un usuario válido."));

        // Una sola consulta trae el ranking de los 3 niveles (ya ordenado por la base de datos).
        List<RankingFila> filas = partidaRepository.ranking();

        List<RankingNivelResponse> respuesta = new ArrayList<>();
        for (enums.Nivel nivel : enums.Nivel.values()) {
            respuesta.add(armarNivel(nivel, filas, usuario.getIdUsuario()));
        }
        return respuesta;
    }

    private RankingNivelResponse armarNivel(enums.Nivel nivel, List<RankingFila> filas, Long idUsuarioActual) {
        List<PosicionResponse> top = new ArrayList<>();
        PosicionResponse miPosicion = null;
        int posicion = 0;

        for (RankingFila fila : filas) {
            if (!fila.getNivel().equals(nivel.name())) continue;

            posicion++;   // las filas vienen ordenadas del más rápido al más lento
            boolean esActual = fila.getUsuarioId().equals(idUsuarioActual);

            PosicionResponse item = new PosicionResponse(posicion, fila.getNombreUsuario(),
                    fila.getMejorTiempoMs(), fila.getPartidasGanadas(), esActual);

            if (posicion <= TAMANO_TOP) top.add(item);
            if (esActual) miPosicion = item;
        }

        return new RankingNivelResponse(nivel.name(), top, miPosicion, posicion);
    }
}
