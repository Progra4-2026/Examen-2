function Ranking({ ranking, cargando }) {
    return (
        <section className="contenido-panel">
            <div className="seccion-titulo"><div><h1>Ranking</h1><p>Mejores tiempos por nivel.</p></div></div>
            {cargando && <p className="estado-vacio">Cargando ranking…</p>}
            {!cargando && <div className="ranking-grid">{ranking.map(nivel => <article className="ranking-card" key={nivel.nivel}>
                <h2>{nivel.nivel}</h2>
                {nivel.top.length === 0 && <p className="texto-suave">Sin victorias todavía.</p>}
                {nivel.top.length > 0 && <table className="datos-tabla"><thead><tr><th>#</th><th>Jugador</th><th>Tiempo</th></tr></thead>
                    <tbody>{nivel.top.map(pos => <tr className={pos.esUsuarioActual ? 'fila-usuario' : ''} key={`${nivel.nivel}-${pos.posicion}`}>
                        <td>{pos.posicion}</td><td>{pos.nombreUsuario}{pos.esUsuarioActual ? ' (tú)' : ''}</td><td>{(pos.mejorTiempoMs / 1000).toFixed(1)} s</td>
                    </tr>)}</tbody></table>}
                {nivel.miPosicion && nivel.miPosicion.posicion > 5 && <p className="mi-posicion">Tu posición: #{nivel.miPosicion.posicion}</p>}
            </article>)}</div>}
        </section>
    );
}
