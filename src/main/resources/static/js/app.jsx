const { useState, useEffect } = React;

function App() {
    const [vista, setVista] = useState('login');
    const [usuario, setUsuario] = useState(null);
    const [partida, setPartida] = useState(null);
    const [niveles, setNiveles] = useState([]);
    const [historial, setHistorial] = useState([]);
    const [ranking, setRanking] = useState([]);
    const [error, setError] = useState(null);
    const [cargando, setCargando] = useState(false);

    useEffect(() => {
        fetch('/api/auth/me', { credentials: 'same-origin' })
            .then(respuesta => respuesta.ok ? respuesta.json() : null)
            .then(actual => {
                if (actual) {
                    setUsuario(actual);
                    setVista('juego');
                }
            })
            .catch(() => setError('No se pudo verificar la sesión.'));
    }, []);

    useEffect(() => {
        if (!usuario) return;
        solicitar('/api/niveles').then(setNiveles).catch(mostrarError);
        solicitar('/api/partidas/actual').then(setPartida).catch(errorActual => {
            if (errorActual.status !== 404) mostrarError(errorActual);
        });
    }, [usuario]);

    useEffect(() => {
        if (!usuario || vista !== 'historial') return;
        setCargando(true);
        solicitar(`/api/usuarios/${usuario.id}/partidas`)
            .then(setHistorial).catch(mostrarError).finally(() => setCargando(false));
    }, [usuario, vista]);

    useEffect(() => {
        if (!usuario || vista !== 'ranking') return;
        setCargando(true);
        solicitar('/api/ranking')
            .then(setRanking).catch(mostrarError).finally(() => setCargando(false));
    }, [usuario, vista]);

    function mostrarError(errorApi) {
        setError(errorApi.message || 'No se pudo completar la operación.');
    }

    async function solicitar(url, opciones = {}) {
        const respuesta = await fetch(url, { credentials: 'same-origin', ...opciones });
        const datos = await respuesta.json().catch(() => ({}));
        if (respuesta.status === 401) {
            setUsuario(null);
            setPartida(null);
            setVista('login');
        }
        if (!respuesta.ok) {
            const errorApi = new Error(datos.error || 'No se pudo completar la operación.');
            errorApi.status = respuesta.status;
            throw errorApi;
        }
        return datos;
    }

    async function enviar(url, cuerpo, alExito) {
        setCargando(true);
        setError(null);
        try {
            const datos = await solicitar(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(cuerpo)
            });
            alExito(datos);
        } catch (e) {
            mostrarError(e);
        } finally {
            setCargando(false);
        }
    }

    function manejarLogin(identificador, contrasena) {
        enviar('/api/auth/login', { identificador, contrasena }, actual => {
            setUsuario(actual);
            setVista('juego');
        });
    }

    function manejarRegistro(datos) {
        enviar('/api/auth/registro', datos, () => {
            setError('Cuenta creada correctamente. Ahora inicia sesión.');
            setVista('login');
        });
    }

    async function nuevaPartida(nivel) {
        setCargando(true);
        setError(null);
        try {
            setPartida(await solicitar('/api/partidas', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nivel })
            }));
        } catch (e) {
            mostrarError(e);
        } finally {
            setCargando(false);
        }
    }

    async function ejecutarAccion(id, tipo, fila, columna) {
        setCargando(true);
        setError(null);
        try {
            setPartida(await solicitar(`/api/partidas/${id}/acciones`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ tipo, fila, columna })
            }));
        } catch (e) {
            mostrarError(e);
        } finally {
            setCargando(false);
        }
    }

    async function verPartida(id) {
        setVista('juego');
        setCargando(true);
        try {
            setPartida(await solicitar(`/api/partidas/${id}`));
        } catch (e) {
            mostrarError(e);
        } finally {
            setCargando(false);
        }
    }

    async function cerrarSesion() {
        try {
            await solicitar('/api/auth/logout', { method: 'POST' });
        } catch (e) {
            mostrarError(e);
        } finally {
            setUsuario(null);
            setPartida(null);
            setVista('login');
        }
    }

    if (!usuario && vista === 'registro') {
        return <Registro onSubmit={manejarRegistro} error={error} cargando={cargando}
            onVolver={() => { setError(null); setVista('login'); }} />;
    }
    if (!usuario) {
        return <Login onSubmit={manejarLogin} error={error} cargando={cargando}
            onIrARegistro={() => { setError(null); setVista('registro'); }} />;
    }

    return <div className="app-shell">
        <Navegacion usuario={usuario} vista={vista} onCambiarVista={vistaNueva => { setError(null); setVista(vistaNueva); }}
            onCerrarSesion={cerrarSesion} />
        <main className="app-contenido">
            {error && <div className="app-alerta" role="alert">{error}<button onClick={() => setError(null)}>×</button></div>}
            {vista === 'juego' && <Tablero partida={partida} niveles={niveles} cargando={cargando}
                onNuevaPartida={nuevaPartida} onAccion={ejecutarAccion} />}
            {vista === 'historial' && <Historial usuario={usuario} historial={historial} cargando={cargando} onVerPartida={verPartida} />}
            {vista === 'ranking' && <Ranking ranking={ranking} cargando={cargando} />}
        </main>
    </div>;
}
