package com.nevi.controller;

import com.nevi.dto.GrupoRequest;
import com.nevi.dto.GrupoResponse;
import com.nevi.service.GrupoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    // GET /api/grupos — Devuelve los grupos del usuario autenticado.
    @GetMapping
    public ResponseEntity<List<GrupoResponse>> misGrupos(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(grupoService.misGrupos(userDetails.getUsername()));
    }

    // POST /api/grupos — Crea un nuevo grupo (solo profesores).
    @PostMapping
    public ResponseEntity<GrupoResponse> crear(
        @Valid @RequestBody GrupoRequest req,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(grupoService.crear(req, userDetails.getUsername()));
    }

    // POST /api/grupos/unirse — Estudiante se une con código de acceso.
    @PostMapping("/unirse")
    public ResponseEntity<GrupoResponse> unirse(
        @RequestBody Map<String, String> body,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        String codigo = body.get("codigo");
        return ResponseEntity.ok(grupoService.unirse(codigo, userDetails.getUsername()));
    }

    // GET /api/grupos/{id} — Obtiene los detalles de un grupo específico.
    @GetMapping("/{id}")
    public ResponseEntity<GrupoResponse> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(grupoService.obtenerPorId(id));
    }
}
