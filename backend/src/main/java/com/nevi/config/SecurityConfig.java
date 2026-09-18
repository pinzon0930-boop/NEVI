package com.nevi.config;

import com.nevi.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desactiva CSRF porque usamos JWT (stateless), no sesiones con cookies.
            .csrf(AbstractHttpConfigurer::disable)

            // Configura CORS para permitir peticiones desde el frontend.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Define qué rutas son públicas y cuáles requieren autenticación.
            // OJO: antes esto era "/api/auth/**" permitAll, lo que dejaba /api/auth/perfil
            // (que SÍ requiere token) público por accidente. Solo register/login son públicos.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()   // Solo login y registro son públicos.
                .requestMatchers("/ws/**").permitAll()         // WebSocket: sin token (el token se pasa en el mensaje).
                .anyRequest().authenticated()                  // Todo lo demás requiere JWT.
            )

            // Sin esto, Spring Security devuelve 403 por defecto ante una petición sin
            // autenticar. ESC-03 (dossier/04-escenarios-calidad.md) exige 401 Unauthorized
            // sin datos expuestos en el cuerpo.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autenticado"))
            )

            // Sesiones stateless — Spring no guarda sesión en servidor, el JWT hace ese trabajo.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Agrega el filtro JWT antes del filtro estándar de usuario/contraseña.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Configuración CORS: permite peticiones desde cualquier origen en desarrollo.
    // En producción, cambiar allowedOrigins a la URL real del frontend.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // BCrypt para hashear contraseñas — el estándar de la industria.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager — necesario para autenticar con email/contraseña.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
        throws Exception {
        return config.getAuthenticationManager();
    }
}
