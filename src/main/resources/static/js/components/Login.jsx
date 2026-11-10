// Formulario de inicio de sesión (solo la parte visual).
// Props:
//   onSubmit(nombreUsuario, contrasena) -> lo llama al enviar el formulario.
//   error          -> mensaje de error a mostrar (o null).
//   cargando       -> true mientras se espera la respuesta del servidor.
//   onIrARegistro  -> lo llama al hacer clic en "Crear cuenta".
// Nota: se usa React.useState y no "const { useState } = React" porque todos los
// .jsx comparten el mismo ámbito global y app.jsx ya declara useState.
function Login({ onSubmit, error, cargando, onIrARegistro }) {
    const [nombreUsuario, setNombreUsuario] = React.useState('');
    const [contrasena, setContrasena] = React.useState('');

    function manejarEnvio(e) {
        e.preventDefault();
        onSubmit(nombreUsuario, contrasena);
    }

    return (
        <div className="auth-pagina">
            <form className="auth-tarjeta" onSubmit={manejarEnvio} noValidate>
                <div className="auth-encabezado">
                    <span className="auth-logo" aria-hidden="true">💣</span>
                    <h1>Buscaminas</h1>
                    <p>Inicia sesión para jugar</p>
                </div>

                {error && (
                    <div className="auth-error" role="alert">{error}</div>
                )}

                <label className="auth-campo">
                    <span>Nombre de usuario</span>
                    <input
                        type="text"
                        name="nombreUsuario"
                        autoComplete="username"
                        value={nombreUsuario}
                        onChange={e => setNombreUsuario(e.target.value)}
                        disabled={cargando}
                        autoFocus
                    />
                </label>

                <label className="auth-campo">
                    <span>Contraseña</span>
                    <input
                        type="password"
                        name="contrasena"
                        autoComplete="current-password"
                        value={contrasena}
                        onChange={e => setContrasena(e.target.value)}
                        disabled={cargando}
                    />
                </label>

                <button type="submit" className="auth-boton" disabled={cargando}>
                    {cargando ? 'Ingresando…' : 'Iniciar sesión'}
                </button>

                <p className="auth-pie">
                    ¿No tienes cuenta?{' '}
                    <button type="button" className="auth-enlace" onClick={onIrARegistro}>
                        Crear cuenta
                    </button>
                </p>
            </form>
        </div>
    );
}
