package com.bachillerato.buscaminas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    // Hash de contraseñas PBKDF2-HMAC-SHA256 (enunciado 4.5).
    // "" = sin pepper, 16 = bytes de sal aleatoria por usuario, 600_000 = iteraciones.
    // Guarda sal + hash en hexadecimal (96 caracteres) en usuario.password_hash.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Pbkdf2PasswordEncoder("", 16, 600_000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
    }

    // Expone el AuthenticationManager que Spring arma con el UserDetailsService y el PasswordEncoder,
    // para que el controlador de login pueda validar usuario y contraseña con authenticate(...).
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Reglas de seguridad que se aplican a cada petición HTTP antes de llegar a los controladores.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Acceso por ruta: se evalúan en orden y gana la primera regla que coincida.
                .authorizeHttpRequests(auth -> auth
                    // Archivos estáticos del frontend (servidos desde resources/static) y la página
                    // de error de Spring Boot, para que los errores no se conviertan en 401.
                    .requestMatchers("/","/index.html","/js/**", "/css/**", "/error").permitAll()
                    // Registro e inicio de sesión: se usan antes de tener sesión.
                    .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login").permitAll()
                    // Lista de niveles: datos fijos, no requiere sesión.
                    .requestMatchers(HttpMethod.GET, "/api/niveles").permitAll()
                    // Todo lo demás (partidas, historial, ranking, me, logout) exige sesión iniciada.
                    .anyRequest().authenticated()
                )
                // El login se hace con JSON en /api/auth/login, no con el formulario HTML de Spring
                // ni con autenticación HTTP Basic.
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // Sin sesión en una ruta protegida: responde 401 en vez de redirigir a una página de login.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .sessionManagement(session -> session
                        .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                )
                // CSRF desactivado: decisión de diseño, justificada en el README.
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
