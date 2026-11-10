package com.bachillerato.buscaminas.model.entity;

import com.bachillerato.buscaminas.model.enums;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "partida")
public class Partida {

    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long idPartida;

    // FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false, length = 20)
    private enums.Nivel nivel;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private enums.EstadoPartida estado;

    @Column(name = "tiempo_ms")
    private Integer tiempoMs;

    @Column(name = "movimientos", nullable = false)
    private int movimientos;

    @Column(name = "estrellas", nullable = false)
    private int estrellas;

    @Column(name = "tablero", columnDefinition = "json", nullable = false)
    private String tablero;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    public Long getIdPartida() { return idPartida; }
    public void setIdPartida(Long idPartida) { this.idPartida = idPartida; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public enums.Nivel getNivel() { return nivel; }
    public void setNivel(enums.Nivel nivel) { this.nivel = nivel; }

    public enums.EstadoPartida getEstado() { return estado; }
    public void setEstado(enums.EstadoPartida estado) { this.estado = estado; }

    public Integer getTiempoMs() { return tiempoMs; }
    public void setTiempoMs(Integer tiempoMs) { this.tiempoMs = tiempoMs; }

    public int getMovimientos() { return movimientos; }
    public void setMovimientos(int movimientos) { this.movimientos = movimientos; }

    public int getEstrellas() { return estrellas; }
    public void setEstrellas(int estrellas) { this.estrellas = estrellas; }

    public String getTablero() { return tablero; }
    public void setTablero(String tablero) { this.tablero = tablero; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    @PrePersist
    void alCrear() {
        if (fechaInicio == null) fechaInicio = LocalDateTime.now();
        if (estado == null) estado = enums.EstadoPartida.EN_CURSO;
    }
}
