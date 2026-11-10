const { useState } = React;   // los hooks salen del objeto global React (no hay import)

function App() {
    const [error, setError] = useState(null);
    const [cargando, setCargando] = useState(false);

    // TODO: llamar a POST /api/auth/login con fetch y async/await,
    // usar setCargando y setError, y guardar el usuario en el estado si responde 200.
    function manejarLogin(nombreUsuario, contrasena) {
        console.log('Login pendiente de implementar:', nombreUsuario);
    }

    // TODO: cambiar a la vista de registro cuando exista ese componente.
    function irARegistro() {
        console.log('Registro pendiente de implementar');
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
