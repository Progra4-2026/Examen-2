package com.bachillerato.buscaminas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegistroRequest(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 80, message = "El nombre no puede tener más de 80 caracteres")
            String nombre,

            @NotBlank(message = "El nombre de usuario es obligatorio")
            @Size(max = 40, message = "El nombre de usuario no puede tener más de 40 caracteres")
            String nombreUsuario,

            @NotBlank(message = "El correo electrónico es obligatorio")
            @Email(message = "El correo electrónico no tiene un formato válido")
            @Size(max = 120, message = "El correo electrónico no puede tener más de 120 caracteres")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                    message = "La contraseña debe contener al menos una letra y un número"
                    )
            String contrasena,

            @NotBlank(message = "La confirmación de la contraseña es obligatoria")
            String confirmacionContrasena
    ) { }

    public record LoginRequest(
            @NotBlank(message = "El nombre de usuario o correo es obligatorio")
            String identificador,

            @NotBlank(message = "La contraseña es obligatoria")
            String contrasena
    ) { }

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
