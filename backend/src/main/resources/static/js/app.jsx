const { useState } = React;   // los hooks salen del objeto global React (no hay import)

function App() {
    const [clics, setClics] = useState(0);

    return (
        <div>
            <h1>Buscaminas</h1>
            <button onClick={() => setClics(clics + 1)}>Clics: {clics}</button>
        </div>
    );
}