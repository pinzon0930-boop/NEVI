package com.nevi.controller;

import com.nevi.dto.MensajeResponse;
import com.nevi.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajeService;

    // ── REST ──────────────────────────────────────────────────────────────────

    // GET /api/mensajes?grupoId=xxx — Obtiene los últimos 50 mensajes.
    @GetMapping("/api/mensajes")
    @ResponseBody
    public ResponseEntity<List<MensajeResponse>> obtener(@RequestParam UUID grupoId) {
        return ResponseEntity.ok(mensajeService.obtener(grupoId));
    }

    // POST /api/mensajes — Envía un mensaje vía REST (también hace broadcast WebSocket).
    @PostMapping("/api/mensajes")
    @ResponseBody
    public ResponseEntity<MensajeResponse> enviarRest(
        @RequestBody Map<String, String> body,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID grupoId   = UUID.fromString(body.get("grupoId"));
        String contenido = body.get("contenido");
        return ResponseEntity.ok(mensajeService.enviar(grupoId, userDetails.getUsername(), contenido));
    }

    // ── WebSocket STOMP ───────────────────────────────────────────────────────
    // El frontend puede enviar mensajes directamente vía WebSocket en lugar de REST.
    // Destino: /app/chat/{grupoId}  →  broadcast a: /topic/grupo/{grupoId}

    @MessageMapping("/chat/{grupoId}")
    public void enviarWs(
        @DestinationVariable UUID grupoId,
        @Payload Map<String, String> body,
        Principal principal  // Spring inyecta el usuario autenticado del WebSocket.
    ) {
        String contenido = body.get("contenido");
        if (principal != null && contenido != null && !contenido.isBlank()) {
            mensajeService.enviar(grupoId, principal.getName(), contenido);
        }
    }
}
