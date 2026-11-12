package com.bachillerato.buscaminas.service;

import com.bachillerato.buscaminas.dto.PartidaDtos.AccionRequest;
import com.bachillerato.buscaminas.dto.PartidaDtos.PartidaResponse;
import com.bachillerato.buscaminas.excepcion.ApiException;
import com.bachillerato.buscaminas.model.Tablero;
import com.bachillerato.buscaminas.model.entity.Partida;
import com.bachillerato.buscaminas.model.entity.Usuario;
import com.bachillerato.buscaminas.model.enums;
import com.bachillerato.buscaminas.repository.PartidaRepository;
import com.bachillerato.buscaminas.repository.UsuarioRepository;

import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
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

    // GET /api/partidas/actual
    // Devuelve la partida EN_CURSO del usuario (por ejemplo, para retomarla al recargar la página).
    @Transactional(readOnly = true)
    public PartidaResponse obtenerPartidaActual(String nombreUsuario) {
        Usuario usuario = buscarUsuario(nombreUsuario);

        Partida partida = partidaRepository.ultimaPartidaPorEstado(usuario.getIdUsuario(),
                        enums.EstadoPartida.EN_CURSO, Limit.of(1))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "No tienes una partida en curso."));

        Tablero tablero = tableroConverter.desdeJson(partida.getTablero());
        return PartidaResponse.desde(partida, tablero);
    }

    // POST /api/partidas/{id}/acciones
    // Revela una casilla o alterna una bandera, y decide si la partida terminó.
    @Transactional
    public PartidaResponse procesarAccion(String nombreUsuario, Long idPartida, AccionRequest accion) {
        // 1. ¿Existe? -> 404.  ¿Es suya? -> 403.
        Partida partida = buscarPartidaPropia(nombreUsuario, idPartida);

        // 2. Validaciones de la acción -> 400.
        if (partida.getEstado() != enums.EstadoPartida.EN_CURSO) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La partida ya no está activa.");
        }

        Tablero tablero = tableroConverter.desdeJson(partida.getTablero());
        int fila = accion.fila();
        int columna = accion.columna();

        if (!tablero.dentroDelTablero(fila, columna)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La casilla está fuera del tablero.");
        }
        if (tablero.estaRevelada(fila, columna)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La casilla ya está revelada.");
        }

        // 3. Ejecutar la acción.
        if (accion.tipo() == enums.TipoAccion.BANDERA) {
            if (tablero.tieneBandera(fila, columna)) {
                tablero.quitarBandera(fila, columna);
            } else {
                tablero.ponerBandera(fila, columna);
            }
        } else {
            if (tablero.tieneBandera(fila, columna)) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "La casilla tiene bandera; quítala antes de revelarla.");
            }

            if (tablero.esMina(fila, columna)) {
                tablero.revelar(fila, columna);
                terminarPartida(partida, false);          // derrota
            } else {
                abrirEnCadena(tablero, fila, columna);
                if (tablero.todasLasSegurasReveladas()) {
                    terminarPartida(partida, true);       // victoria
                }
            }
        }

        // 4. Guardar. Al estar dentro de @Transactional, Hibernate también detectaría los cambios
        //    solo (dirty checking), pero el save deja explícito que aquí se persiste.
        //    @Version en Partida: si llegan dos acciones a la vez sobre la misma partida,
        //    la segunda falla con un conflicto en lugar de pisar los cambios de la primera.
        partida.setMovimientos(partida.getMovimientos() + 1);
        partida.setTablero(tableroConverter.aJson(tablero));
        partida = partidaRepository.save(partida);

        return PartidaResponse.desde(partida, tablero);
    }

    // APERTURA EN CADENA (flood fill).
    // Revela la casilla indicada; si tiene 0 minas alrededor, revela también sus vecinas,
    // y repite con cada vecina que también tenga 0. Las casillas con número (1-8) se revelan
    // pero NO siguen la cadena. Las casillas con bandera no se abren automáticamente.
    //
    // Se usa una cola (búsqueda en anchura) en vez de recursión, así un tablero grande
    // no puede provocar StackOverflowError.
    //
    // Es pública porque no depende de la base de datos: la prueba del paso 6 la llama
    // directamente con un tablero armado a mano.
    public void abrirEnCadena(Tablero tablero, int filaInicial, int columnaInicial) {
        Deque<int[]> pendientes = new ArrayDeque<>();
        pendientes.add(new int[]{filaInicial, columnaInicial});

        while (!pendientes.isEmpty()) {
            int[] casilla = pendientes.poll();
            int fila = casilla[0];
            int columna = casilla[1];

            // Saltar lo que no se debe abrir: fuera del tablero, ya abierta, con bandera o mina.
            if (!tablero.dentroDelTablero(fila, columna)
                    || tablero.estaRevelada(fila, columna)
                    || tablero.tieneBandera(fila, columna)
                    || tablero.esMina(fila, columna)) {
                continue;
            }

            tablero.revelar(fila, columna);

            // Solo las casillas vacías (0) propagan la cadena a sus 8 vecinas.
            if (tablero.minasAdyacentes(fila, columna) == 0) {
                for (int df = -1; df <= 1; df++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (df != 0 || dc != 0) {
                            pendientes.add(new int[]{fila + df, columna + dc});
                        }
                    }
                }
            }
        }
    }

    // Cierra la partida. El tiempo se mide en el servidor: desde fechaInicio hasta ahora.
    // Victoria: tiempo_ms y estrellas (entra al ranking).
    // Derrota:  tiempo_ms = NULL y 0 estrellas (vw_ranking la excluye por "tiempo_ms IS NOT NULL").
    private void terminarPartida(Partida partida, boolean gano) {
        LocalDateTime fin = LocalDateTime.now();
        partida.setEstado(enums.EstadoPartida.TERMINADA);
        partida.setFechaFin(fin);

        if (gano) {
            long tiempoMs = Duration.between(partida.getFechaInicio(), fin).toMillis();
            partida.setTiempoMs((int) tiempoMs);
            partida.setEstrellas(calcularEstrellas(partida.getNivel(), tiempoMs));
        } else {
            partida.setTiempoMs(null);
            partida.setEstrellas(0);
        }
    }

    // Regla propia (el enunciado solo pide 0 a 3): según un tiempo objetivo por nivel.
    // 3 estrellas dentro del objetivo, 2 hasta el doble, 1 si tardó más.
    private int calcularEstrellas(enums.Nivel nivel, long tiempoMs) {
        long objetivoMs = switch (nivel) {
            case PRINCIPIANTE -> 60_000;     // 1 minuto
            case INTERMEDIO -> 240_000;      // 4 minutos
            case EXPERTO -> 600_000;         // 10 minutos
        };
        if (tiempoMs <= objetivoMs) return 3;
        if (tiempoMs <= objetivoMs * 2) return 2;
        return 1;
    }

    // GET /api/partidas/{id}
    // Detalle de una partida del usuario (normalmente una terminada, desde el historial).
    // Pasa por el mismo filtro que todo lo demás: si la partida sigue EN_CURSO, las minas no se envían.
    @Transactional(readOnly = true)
    public PartidaResponse obtenerPartida(String nombreUsuario, Long idPartida) {
        Partida partida = buscarPartidaPropia(nombreUsuario, idPartida);
        Tablero tablero = tableroConverter.desdeJson(partida.getTablero());
        return PartidaResponse.desde(partida, tablero);
    }

    // Busca una partida y verifica que sea del usuario de la sesión.
    // Se separan los dos casos a propósito: primero se busca solo por id (si no existe -> 404),
    // y después se compara el dueño (si es de otro -> 403).
    private Partida buscarPartidaPropia(String nombreUsuario, Long idPartida) {
        Usuario usuario = buscarUsuario(nombreUsuario);

        Partida partida = partidaRepository.findById(idPartida)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La partida no existe."));

        if (!partida.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "La partida pertenece a otro usuario.");
        }
        return partida;
    }

    private Usuario buscarUsuario(String nombreUsuario) {
        return usuarioRepository.buscarPorNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "La sesión no corresponde a un usuario válido."));
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
