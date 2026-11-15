function Tablero({ partida, niveles, cargando, onNuevaPartida, onAccion }) {
    const [nivel, setNivel] = React.useState('PRINCIPIANTE');
    const [ahora, setAhora] = React.useState(Date.now());

    React.useEffect(() => {
        if (!partida || partida.estado !== 'EN_CURSO') return undefined;
        const reloj = setInterval(() => setAhora(Date.now()), 1000);
        return () => clearInterval(reloj);
    }, [partida]);

    function tiempoVisible() {
        if (!partida) return '00:00';
        const fin = partida.fechaFin ? new Date(partida.fechaFin).getTime() : ahora;
        const inicio = new Date(partida.fechaInicio).getTime();
        const segundos = Math.max(0, Math.floor((fin - inicio) / 1000));
        return `${String(Math.floor(segundos / 60)).padStart(2, '0')}:${String(segundos % 60).padStart(2, '0')}`;
    }

    function claseCasilla(valor) {
        if (valor === 'OCULTA') return 'casilla oculta';
        if (valor === 'BANDERA') return 'casilla bandera';
        if (valor === 'MINA') return 'casilla mina';
        return `casilla abierta numero-${valor}`;
    }

    function contenidoCasilla(valor) {
        if (valor === 'BANDERA') return '⚑';
        if (valor === 'MINA') return '💣';
        if (valor === 'OCULTA') return '';
        return valor;
    }

    return (
        <section className="juego-panel">
            <div className="seccion-titulo">
                <div><h1>Tu partida</h1><p>Descubre todas las casillas sin minas.</p></div>
                <div className="juego-controles">
                    <select value={nivel} onChange={e => setNivel(e.target.value)} disabled={!!partida && partida.estado === 'EN_CURSO'}>
                        {niveles.map(item => <option key={item.nombre} value={item.nombre}>{item.nombre}</option>)}
                    </select>
                    <button className="auth-boton boton-pequeno" onClick={() => onNuevaPartida(nivel)} disabled={cargando || (partida && partida.estado === 'EN_CURSO')}>
                        Nueva partida
                    </button>
                </div>
            </div>
            {!partida && <div className="estado-vacio">Elige un nivel y presiona <strong>Nueva partida</strong> para comenzar.</div>}
            {partida && <div className="partida-contenido">
                <div className="partida-info">
                    <span><strong>Nivel:</strong> {partida.nivel}</span>
                    <span><strong>Minas:</strong> {partida.numMinas - partida.banderasColocadas}/{partida.numMinas}</span>
                    <span><strong>Movimientos:</strong> {partida.movimientos}</span>
                    <span><strong>Tiempo:</strong> {tiempoVisible()}</span>
                    {partida.estado !== 'EN_CURSO' && <strong className={partida.resultado === 'VICTORIA' ? 'resultado-ok' : 'resultado-error'}>
                        {partida.resultado === 'VICTORIA' ? '¡Victoria!' : 'Partida terminada'}
                    </strong>}
                </div>
                <p className="instrucciones">Clic para revelar · clic derecho para colocar o quitar una bandera</p>
                <div className="tablero" style={{ '--columnas': partida.columnas }}>
                    {partida.casillas.flatMap((fila, f) => fila.map((valor, c) =>
                        <button key={`${f}-${c}`} type="button" className={claseCasilla(valor)}
                            disabled={cargando || partida.estado !== 'EN_CURSO' || valor !== 'OCULTA' && valor !== 'BANDERA'}
                            onClick={() => onAccion(partida.id, 'REVELAR', f, c)}
                            onContextMenu={e => { e.preventDefault(); onAccion(partida.id, 'BANDERA', f, c); }}>
                            {contenidoCasilla(valor)}
                        </button>
                    ))}
                </div>
            </div>}
        </section>
    );
}
