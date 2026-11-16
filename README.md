# Buscaminas

Proyecto de Buscaminas para el Examen 2 de Programación 4. El backend está
desarrollado con Spring Boot 3.5.3, Java 17 y MySQL. El frontend está incluido
en el mismo proyecto como archivos estáticos (React cargado desde CDN), por lo
que no requiere Node.js ni un servidor frontend separado.

## Requisitos

- Java Development Kit (JDK) 17 o superior.
- MySQL Community Server 8.0 o superior (MariaDB 10.5 o superior también es
  compatible con `minas.sql`).
- Git, para clonar el repositorio.
- Windows PowerShell, macOS Terminal o una terminal Linux.

## 1. Descargar el proyecto

Clonar el repositorio y entrar en la carpeta:

```powershell
git clone https://github.com/Progra4-2026/Examen-2.git
cd Examen-2
```

En macOS/Linux se usan los mismos comandos.

## 2. Instalar y configurar MySQL

### Windows

1. Descargar **MySQL Installer for Windows (Community)** desde
   [dev.mysql.com/downloads/installer](https://dev.mysql.com/downloads/installer/).
2. Ejecutar el instalador y seleccionar **Developer Default** (o **Server
   only** si solo se desea el servidor).
3. En **Type and Networking**, dejar:
   - Configuración: `Development Computer`.
   - Protocolo: `TCP/IP`.
   - Puerto: `3306`.
4. En **Authentication Method**, mantener la autenticación recomendada para
   MySQL 8.
5. En **Accounts and Roles**, definir una contraseña para el usuario `root` y
   conservarla para la configuración del proyecto.
6. Finalizar la instalación dejando el servicio de MySQL configurado para
   iniciarse con Windows.

Comprobar que el cliente está disponible:

```powershell
mysql --version
```

Si `mysql` no se reconoce, abrir una nueva terminal o ejecutar el cliente
usando su ruta completa, normalmente:

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" --version
```

### macOS/Linux

Instalar **MySQL Community Server** desde
[dev.mysql.com/downloads/mysql](https://dev.mysql.com/downloads/mysql/) o
mediante el gestor de paquetes del sistema. Durante la instalación definir la
contraseña de `root`, usar el puerto `3306` y verificar:

```bash
mysql --version
```

## 3. Crear la base de datos y cargar `minas.sql`

Desde la raíz del proyecto, ejecutar el script con el usuario administrador de
MySQL. El comando solicitará la contraseña de `root`:

```powershell
Get-Content .\minas.sql | mysql -u root -p
```

En macOS/Linux:

```bash
mysql -u root -p < minas.sql
```

El script crea la base `minas`, sus tablas, la vista de ranking y los datos de
prueba. También elimina y vuelve a crear las tablas existentes, por lo que
restablece las partidas y usuarios cada vez que se ejecuta.

Verificar la carga:

```powershell
mysql -u root -p -e "USE minas; SHOW TABLES; SELECT nombre_usuario, rol FROM usuario;"
```

La salida debe incluir `usuario`, `partida`, `vw_ranking`, y los usuarios
`docente` y `jugador1`.

## 4. Configurar la conexión del backend

Crear el archivo `.env` en la raíz copiando `.env.example`:

```powershell
Copy-Item .env.example .env
```

En macOS/Linux:

```bash
cp .env.example .env
```

Editar `.env` y colocar la contraseña definida para MySQL:

```properties
DB_USER=root
DB_PASSWORD=TU_CONTRASENA_DE_MYSQL
```

Si MySQL está en otro equipo o usa otro puerto, agregar o modificar:

```properties
DB_HOST=localhost
DB_PORT=3306
```

La aplicación lee estas variables desde `.env`. Si no se especifican
`DB_HOST`, `DB_PORT` o `DB_USER`, utiliza `localhost`, `3306` y `root`,
respectivamente. No subir `.env` al repositorio: contiene credenciales locales y
está excluido por `.gitignore`.

## 5. Ejecutar backend y frontend

No se ejecutan dos procesos separados. Spring Boot inicia el backend y sirve
simultáneamente el frontend estático desde `src/main/resources/static`.

En Windows, desde la raíz del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

En macOS/Linux:

```bash
./mvnw spring-boot:run
```

Cuando aparezca el mensaje de que la aplicación inició, abrir:

<http://localhost:8080/>

Para generar y ejecutar el `.jar`:

```powershell
.\mvnw.cmd clean package
java -jar .\target\buscaminas-0.0.1-SNAPSHOT.jar
```

En macOS/Linux:

```bash
./mvnw clean package
java -jar target/buscaminas-0.0.1-SNAPSHOT.jar
```

La API queda disponible bajo `http://localhost:8080/api`. El navegador debe
abrirse en el mismo puerto para que el frontend y la API compartan la sesión.

## 6. Credenciales de prueba

Estas cuentas son creadas por `minas.sql`. La contraseña de ambas es
`Prueba123$`.

| Usuario | Correo | Rol | Contraseña |
|---|---|---|---|
| `docente` | `docente@minas.local` | ADMIN | `Prueba123$` |
| `jugador1` | `jugador1@minas.local` | JUGADOR | `Prueba123$` |

En la pantalla de inicio de sesión se puede usar el nombre de usuario o el
correo. El script también inserta una partida terminada de ejemplo para que el
ranking tenga datos desde el primer arranque.

## 7. Verificación rápida

Con la aplicación ejecutándose, comprobar que responde:

```powershell
Invoke-WebRequest http://localhost:8080/ -UseBasicParsing
Invoke-RestMethod http://localhost:8080/api/niveles
```

También se puede verificar el login desde PowerShell:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/auth/login `
  -ContentType "application/json" `
  -Body '{"identificador":"jugador1","contrasena":"Prueba123$"}'
```

## 8. Ejecutar las pruebas

Con MySQL configurado y la base `minas` cargada, ejecutar toda la suite:

```powershell
.\mvnw.cmd test
```

En macOS/Linux:

```bash
./mvnw test
```

La prueba de contexto (`BuscaminasApplicationTests`) necesita conectarse a
MySQL porque carga la aplicación completa. Para ejecutar únicamente las
pruebas aisladas del servicio, sin levantar el contexto ni conectarse a la
base, usar:

```powershell
.\mvnw.cmd -Dtest=PartidaServiceTest test
```

En macOS/Linux:

```bash
./mvnw -Dtest=PartidaServiceTest test
```

## Solución de problemas

- **`Communications link failure`**: comprobar que el servicio MySQL está
  iniciado, que el puerto de `.env` es correcto y que la contraseña pertenece
  al usuario configurado.
- **`Unknown database 'minas'`**: volver a ejecutar `minas.sql` desde la raíz
  del proyecto.
- **El puerto 8080 está ocupado**: detener el proceso que lo usa o cambiar
  `server.port` en `src/main/resources/application.properties`; en ese caso,
  abrir el frontend con el nuevo puerto.
- **El login no funciona después de recargar la base**: ejecutar nuevamente
  `minas.sql`, ya que el script restablece los usuarios semilla y sus hashes.
