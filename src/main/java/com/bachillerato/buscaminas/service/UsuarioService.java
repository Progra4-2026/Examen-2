package com.bachillerato.buscaminas.service;

import com.bachillerato.buscaminas.dto.AuthDtos.RegistroRequest;
import com.bachillerato.buscaminas.excepcion.ApiException;
import com.bachillerato.buscaminas.model.entity.Usuario;
import com.bachillerato.buscaminas.model.enums;
import com.bachillerato.buscaminas.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Registra un jugador nuevo. La verificación de duplicados y el guardado
    // ocurren en la misma transacción.
    @Transactional
    public Usuario registrar(RegistroRequest request) {
        String nombre = request.nombre().trim();
        String nombreUsuario = request.nombreUsuario().trim();
        String email = request.email().trim().toLowerCase();

        if (!request.contrasena().equals(request.confirmacionContrasena())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden.");
        }

        if (usuarioRepository.existeNombreUsuario(nombreUsuario)
                || usuarioRepository.existeEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "El nombre de usuario o el correo ya están registrados.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.contrasena()));
        usuario.setRol(enums.Rol.JUGADOR);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    // Busca al usuario autenticado por su nombre de usuario (el "name" de la sesión).
    @Transactional(readOnly = true)
    public Usuario buscarPorNombreUsuario(String nombreUsuario) {
        return usuarioRepository.buscarPorNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                        "La sesión no corresponde a un usuario válido."));
    }
}
