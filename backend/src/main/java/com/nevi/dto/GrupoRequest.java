package com.nevi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GrupoRequest {
    @NotBlank(message = "El nombre del grupo es obligatorio")
    private String nombre;
    private String descripcion;
}
