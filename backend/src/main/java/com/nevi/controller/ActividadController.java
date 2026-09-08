package com.nevi.controller;

import com.nevi.dto.ActividadRequest;
import com.nevi.dto.ActividadResponse;
import com.nevi.service.ActividadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadService actividadService;

    // GET /api/actividades?grupoId=xxx — Lista las actividades de un grupo.
    @GetMapping
    public ResponseEntity<List<ActividadResponse>> obtener(@RequestParam UUID grupoId) {
        return ResponseEntity.ok(actividadService.obtener(grupoId));
    }

    // POST /api/actividades — Crea una nueva actividad (solo profesores).
    @PostMapping
    public ResponseEntity<ActividadResponse> crear(
        @Valid @RequestBody ActividadRequest req,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(actividadService.crear(req, userDetails.getUsername()));
    }

    // POST /api/actividades/{id}/entregar — Estudiante entrega una actividad.
    @PostMapping("/{id}/entregar")
    public ResponseEntity<Void> entregar(
        @PathVariable UUID id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        actividadService.entregar(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // GET /api/actividades/mis-entregas — IDs de actividades entregadas por el estudiante.
    @GetMapping("/mis-entregas")
    public ResponseEntity<List<UUID>> misEntregas(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(actividadService.misEntregas(userDetails.getUsername()));
    }
}
