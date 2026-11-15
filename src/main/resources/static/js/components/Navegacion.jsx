function Navegacion({ usuario, vista, onCambiarVista, onCerrarSesion }) {
    return (
        <header className="app-barra">
            <div className="app-marca">💣 <strong>Buscaminas</strong></div>
            <nav className="app-nav" aria-label="Navegación principal">
                <button className={vista === 'juego' ? 'nav-activo' : ''} onClick={() => onCambiarVista('juego')}>Jugar</button>
                <button className={vista === 'historial' ? 'nav-activo' : ''} onClick={() => onCambiarVista('historial')}>Historial</button>
                <button className={vista === 'ranking' ? 'nav-activo' : ''} onClick={() => onCambiarVista('ranking')}>Ranking</button>
            </nav>
            <div className="app-usuario">
                <span>{usuario.nombreUsuario}</span>
                <button className="nav-salir" onClick={onCerrarSesion}>Salir</button>
            </div>
        </header>
    );
}
