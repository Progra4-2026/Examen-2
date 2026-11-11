function Registro({ onSubmit, error, cargando, onVolver }) {
    const [datos, setDatos] = React.useState({
        nombre: '', nombreUsuario: '', email: '', contrasena: '', confirmacionContrasena: ''
    });

    function actualizar(campo, valor) {
        setDatos(actuales => ({ ...actuales, [campo]: valor }));
    }

    function manejarEnvio(e) {
        e.preventDefault();
        onSubmit(datos);
    }

    return (
        <div className="auth-pagina">
            <form className="auth-tarjeta" onSubmit={manejarEnvio}>
                <div className="auth-encabezado">
                    <span className="auth-logo" aria-hidden="true">💣</span>
                    <h1>Crear cuenta</h1>
                    <p>Regístrate para guardar tus partidas</p>
                </div>
                {error && <div className="auth-error" role="alert">{error}</div>}
                <label className="auth-campo"><span>Nombre completo</span>
                    <input type="text" name="nombre" autoComplete="name" required maxLength="80"
                        value={datos.nombre} onChange={e => actualizar('nombre', e.target.value)} disabled={cargando} autoFocus />
                </label>
                <label className="auth-campo"><span>Nombre de usuario</span>
                    <input type="text" name="nombreUsuario" autoComplete="username" required maxLength="40"
                        value={datos.nombreUsuario} onChange={e => actualizar('nombreUsuario', e.target.value)} disabled={cargando} />
                </label>
                <label className="auth-campo"><span>Correo electrónico</span>
                    <input type="email" name="email" autoComplete="email" required maxLength="120"
                        value={datos.email} onChange={e => actualizar('email', e.target.value)} disabled={cargando} />
                </label>
                <label className="auth-campo"><span>Contraseña</span>
                    <input type="password" name="contrasena" autoComplete="new-password" required minLength="8"
                        pattern="(?=.*[A-Za-z])(?=.*\d).+" title="Debe contener al menos una letra y un número"
                        value={datos.contrasena} onChange={e => actualizar('contrasena', e.target.value)} disabled={cargando} />
                </label>
                <label className="auth-campo"><span>Confirmar contraseña</span>
                    <input type="password" name="confirmacionContrasena" autoComplete="new-password" required minLength="8"
                        value={datos.confirmacionContrasena} onChange={e => actualizar('confirmacionContrasena', e.target.value)} disabled={cargando} />
                </label>
                <p className="auth-ayuda">Mínimo 8 caracteres, con al menos una letra y un número.</p>
                <button type="submit" className="auth-boton" disabled={cargando}>
                    {cargando ? 'Creando cuenta…' : 'Crear cuenta'}
                </button>
                <p className="auth-pie">¿Ya tienes cuenta?{' '}
                    <button type="button" className="auth-enlace" onClick={onVolver}>Iniciar sesión</button>
                </p>
            </form>
        </div>
    );
}
