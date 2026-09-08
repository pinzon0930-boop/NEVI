package com.nevi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ActividadRequest {
    @NotNull
    private UUID grupoId;

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    // Fecha límite en formato ISO 8601 (ej. "2025-12-31T23:59:00"). Puede ser null.
    private String fechaEntrega;
}
