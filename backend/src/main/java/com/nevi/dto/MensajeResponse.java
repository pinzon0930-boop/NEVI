package com.nevi.dto;

import com.nevi.entity.Mensaje;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

// DTO del mensaje — mismo formato que el frontend esperaba de Supabase.
// Se usa tanto en la respuesta REST como en el broadcast de WebSocket.
@Data
public class MensajeResponse {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private UUID userId;
    private UserInfo users;   // Mantiene el nombre "users" para compatibilidad con el frontend original.

    @Data
    public static class UserInfo {
        private String name;
        private String role;
    }

    public static MensajeResponse desde(Mensaje m) {
        MensajeResponse r = new MensajeResponse();
        r.id        = m.getId();
        r.content   = m.getContent();
        r.createdAt = m.getCreatedAt();
        r.userId    = m.getUser().getId();

        UserInfo ui = new UserInfo();
        ui.name = m.getUser().getName();
        ui.role = m.getUser().getRole();
        r.users = ui;

        return r;
    }
}
