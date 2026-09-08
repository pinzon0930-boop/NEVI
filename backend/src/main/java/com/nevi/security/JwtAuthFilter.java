package com.nevi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Filtro que se ejecuta en cada petición HTTP para verificar el token JWT.
// Si el token es válido, autentica al usuario en el contexto de seguridad de Spring.
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {

        // Lee el header Authorization de la petición.
        String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", continúa sin autenticar.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Extrae el token (todo después de "Bearer ").
        String token = authHeader.substring(7);

        // Verifica que el token sea válido y que no haya autenticación previa.
        if (jwtUtil.esValido(token) && SecurityContextHolder.getContext().getAuthentication() == null) {

            String email = jwtUtil.obtenerEmail(token);

            // Carga los detalles del usuario desde la base de datos.
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Crea el objeto de autenticación y lo establece en el contexto de seguridad.
            var auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Continúa con el siguiente filtro en la cadena.
        chain.doFilter(request, response);
    }
}
