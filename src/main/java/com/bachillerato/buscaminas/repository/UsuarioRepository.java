package com.bachillerato.buscaminas.repository;

import com.bachillerato.buscaminas.model.entity.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Login: busca un usuario por su nombre de usuario.
    // Usa: nombreUsuario (único en la tabla).
    // Devuelve: el usuario completo (incluye passwordHash, rol y activo), o vacío si no existe.
    @Query("""
            SELECT u
            FROM Usuario u
            WHERE u.nombreUsuario = :nombreUsuario
            """)
    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);

    // Login alternativo: busca un usuario por su email.
    // Usa: email (único en la tabla).
    // Devuelve: el usuario completo, o vacío si no existe.
    @Query("""
            SELECT u
            FROM Usuario u
            WHERE u.email = :email
            """)
    Optional<Usuario> buscarPorEmail(String email);

    // Registro: verifica si el nombre de usuario ya está tomado antes de guardar.
    // Usa: nombreUsuario.
    // Devuelve: true si ya existe un usuario con ese nombre, false si está libre.
    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM Usuario u
            WHERE u.nombreUsuario = :nombreUsuario
            """)
    boolean existeNombreUsuario(String nombreUsuario);

    // Registro: verifica si el email ya está registrado antes de guardar.
    // Usa: email.
    // Devuelve: true si ya existe un usuario con ese email, false si está libre.
    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM Usuario u
            WHERE u.email = :email
            """)
    boolean existeEmail(String email);
}
