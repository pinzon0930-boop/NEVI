package com.nevi.service;

import com.nevi.dto.AuthResponse;
import com.nevi.dto.LoginRequest;
import com.nevi.dto.RegisterRequest;
import com.nevi.entity.User;
import com.nevi.repository.UserRepository;
import com.nevi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // Registra un nuevo usuario y devuelve el token JWT.
    @Transactional
    public AuthResponse registrar(RegisterRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese email.");
        }

        User user = User.builder()
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword())) // Hashea la contraseña con BCrypt.
            .name(req.getName())
            .role(req.getRole())
            .build();

        userRepository.save(user);

        String token = jwtUtil.generarToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    // Autentica con email/contraseña y devuelve el token JWT.
    public AuthResponse login(LoginRequest req) {

        // Spring Security verifica las credenciales automáticamente.
        // Si son incorrectas, lanza BadCredentialsException.
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String token = jwtUtil.generarToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    // Devuelve el perfil del usuario autenticado.
    public AuthResponse perfil(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return new AuthResponse(null, user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}
