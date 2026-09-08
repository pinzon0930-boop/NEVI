package com.nevi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Utilidad para crear y validar tokens JWT.
@Component
public class JwtUtil {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtUtil(
        @Value("${nevi.jwt.secret}") String secreto,
        @Value("${nevi.jwt.expiration-ms}") long expiracionMs
    ) {
        // Genera la clave HMAC-SHA256 a partir del secreto configurado.
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    // Genera un token JWT que contiene el email del usuario como "subject".
    public String generarToken(String email) {
        return Jwts.builder()
            .subject(email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiracionMs))
            .signWith(clave)
            .compact();
    }

    // Extrae el email (subject) del token.
    public String obtenerEmail(String token) {
        return parsear(token).getPayload().getSubject();
    }

    // Valida si el token es correcto y no expiró. Devuelve false si hay cualquier error.
    public boolean esValido(String token) {
        try {
            parsear(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Parsea el token y lanza excepción si es inválido.
    private Jws<Claims> parsear(String token) {
        return Jwts.parser()
            .verifyWith(clave)
            .build()
            .parseSignedClaims(token);
    }
}
