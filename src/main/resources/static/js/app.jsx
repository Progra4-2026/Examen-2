const { useState, useEffect } = React;

function App() {
    const [vista, setVista] = useState('login');
    const [usuario, setUsuario] = useState(null);
    const [error, setError] = useState(null);
    const [cargando, setCargando] = useState(false);

    useEffect(() => {
        fetch('/api/auth/me', { credentials: 'same-origin' })
            .then(respuesta => respuesta.ok ? respuesta.json() : null)
            .then(usuarioActual => {
                if (usuarioActual) {
                    setUsuario(usuarioActual);
                    setVista('juego');
                }
            })
            .catch(() => setError('No se pudo verificar la sesión. Intenta de nuevo.'));
    }, []);

    async function manejarLogin(identificador, contrasena) {
        await enviar('/api/auth/login', { identificador, contrasena }, usuarioActual => {
            setUsuario(usuarioActual);
            setVista('juego');
        });
    }

    function irARegistro() {
        setError(null);
        setVista('registro');
    }

    async function manejarRegistro(datos) {
        await enviar('/api/auth/registro', datos, () => {
            setError('Cuenta creada correctamente. Ahora inicia sesión.');
            setVista('login');
        });
    }

    async function enviar(url, cuerpo, alExito) {
        setCargando(true);
        setError(null);
        try {
            const respuesta = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify(cuerpo)
            });
            const datos = await respuesta.json().catch(() => ({}));
            if (!respuesta.ok) {
                throw new Error(datos.error || 'No se pudo completar la operación.');
            }
            alExito(datos);
        } catch (e) {
            setError(e.message);
        } finally {
            setCargando(false);
        }
    }

    async function cerrarSesion() {
        await fetch('/api/auth/logout', { method: 'POST', credentials: 'same-origin' });
        setUsuario(null);
        setVista('login');
        setError(null);
    }

    if (vista === 'registro') {
        return <Registro onSubmit={manejarRegistro} error={error} cargando={cargando} onVolver={() => {
            setError(null); setVista('login');
        }} />;
    }
    if (usuario) {
        return <div className="auth-pagina"><section className="auth-tarjeta">
            <div className="auth-encabezado"><span className="auth-logo" aria-hidden="true">💣</span>
                <h1>Bienvenido, {usuario.nombre}</h1><p>Sesión iniciada de forma segura.</p></div>
            <button type="button" className="auth-boton" onClick={cerrarSesion}>Cerrar sesión</button>
        </section></div>;
    }
    return (
        <Login
            onSubmit={manejarLogin}
            error={error}
            cargando={cargando}
            onIrARegistro={irARegistro}
        />
    );
}
