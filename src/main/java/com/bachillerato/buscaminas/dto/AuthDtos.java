package com.bachillerato.buscaminas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegistroRequest(
            @NotBlank @Size(max = 80) String nombre,
            @NotBlank @Size(max = 40) String nombreUsuario,
            @NotBlank @Email @Size(max = 120) String email,
            @NotBlank @Size(min = 8, max = 72)
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                    message = "La contraseña debe contener al menos una letra y un número")
            String contrasena,
            @NotBlank String confirmacionContrasena
    ) {
    }

    public record LoginRequest(
            @NotBlank String identificador,
            @NotBlank String contrasena
    ) {
    }

    public record UsuarioResponse(
            Long id,
            String nombre,
            String nombreUsuario,
            String email,
            String rol
    ) {
        public static UsuarioResponse desde(com.bachillerato.buscaminas.model.entity.Usuario usuario) {
            return new UsuarioResponse(usuario.getIdUsuario(), usuario.getNombre(),
                    usuario.getNombreUsuario(), usuario.getEmail(), usuario.getRol().name());
        }
    }
}
