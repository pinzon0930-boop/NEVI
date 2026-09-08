package com.nevi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

// Respuesta que el servidor devuelve al hacer login o registro exitoso.
// Contiene el token JWT y los datos básicos del usuario.
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UUID id;
    private String email;
    private String name;
    private String role;
}
