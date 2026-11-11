package com.bachillerato.buscaminas.controller;

import com.bachillerato.buscaminas.dto.AuthDtos.LoginRequest;
import com.bachillerato.buscaminas.dto.AuthDtos.RegistroRequest;
import com.bachillerato.buscaminas.dto.AuthDtos.UsuarioResponse;
import com.bachillerato.buscaminas.model.entity.Usuario;
import com.bachillerato.buscaminas.model.enums;
import com.bachillerato.buscaminas.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String ERROR_CREDENCIALES = "Usuario o contraseña incorrectos.";

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(UsuarioRepository usuarioRepository,
                          AuthenticationManager authenticationManager,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest request) {
        String nombreUsuario = request.nombreUsuario().trim();
        String email = request.email().trim().toLowerCase();
        if (!request.contrasena().equals(request.confirmacionContrasena())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden."));
        }
        if (usuarioRepository.existeNombreUsuario(nombreUsuario)
                || usuarioRepository.existeEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El nombre de usuario o el correo ya están registrados."));
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(request.contrasena()));
        usuario.setRol(enums.Rol.JUGADOR);
        usuario.setActivo(true);
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UsuarioResponse.desde(usuarioRepository.save(usuario)));
        } catch (DataIntegrityViolationException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El nombre de usuario o el correo ya están registrados."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.identificador().trim(), request.contrasena()));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, httpRequest, httpResponse);
            Usuario usuario = usuarioRepository.buscarPorNombreUsuario(authentication.getName())
                    .orElseThrow(() -> new BadCredentialsException(ERROR_CREDENCIALES));
            return ResponseEntity.ok(UsuarioResponse.desde(usuario));
        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ERROR_CREDENCIALES));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        return UsuarioResponse.desde(usuarioRepository.buscarPorNombreUsuario(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException(ERROR_CREDENCIALES)));
    }

}
