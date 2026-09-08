package com.nevi.controller;

import com.nevi.dto.AuthResponse;
import com.nevi.dto.LoginRequest;
import com.nevi.dto.RegisterRequest;
import com.nevi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register — Registra un nuevo usuario.
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.registrar(req));
    }

    // POST /api/auth/login — Inicia sesión y devuelve el token JWT.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // GET /api/auth/perfil — Devuelve el perfil del usuario autenticado (requiere token).
    @GetMapping("/perfil")
    public ResponseEntity<AuthResponse> perfil(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.perfil(userDetails.getUsername()));
    }
}
