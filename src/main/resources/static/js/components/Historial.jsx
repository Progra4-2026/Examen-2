function Historial({ usuario, historial, cargando, onVerPartida }) {
    function fecha(valor) {
        return valor ? new Date(valor).toLocaleString('es-CR') : '—';
    }
    return (
        <section className="contenido-panel">
            <div className="seccion-titulo"><div><h1>Historial de partidas</h1><p>Tus resultados guardados en el servidor.</p></div></div>
            {cargando && <p className="estado-vacio">Cargando historial…</p>}
            {!cargando && historial.length === 0 && <p className="estado-vacio">Aún no tienes partidas registradas.</p>}
            {!cargando && historial.length > 0 && <div className="tabla-scroll"><table className="datos-tabla">
                <thead><tr><th>Fecha</th><th>Nivel</th><th>Resultado</th><th>Tiempo</th><th>Movimientos</th><th></th></tr></thead>
                <tbody>{historial.map(item => <tr key={item.id}>
                    <td>{fecha(item.fechaInicio)}</td><td>{item.nivel}</td>
                    <td><span className={`etiqueta ${item.resultado === 'VICTORIA' ? 'etiqueta-ok' : 'etiqueta-error'}`}>{item.resultado || item.estado}</span></td>
                    <td>{item.tiempoMs ? `${(item.tiempoMs / 1000).toFixed(1)} s` : '—'}</td>
                    <td>{item.movimientos}</td><td><button className="tabla-enlace" onClick={() => onVerPartida(item.id)}>Ver</button></td>
                </tr>)}</tbody>
            </table></div>}
        </section>
    );
}
