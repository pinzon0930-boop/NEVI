package com.nevi.dto;

import com.nevi.entity.Grupo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

// DTO de respuesta para grupos — evita exponer la entidad JPA directamente.
@Data
public class GrupoResponse {
    private UUID id;
    private String name;
    private String description;
    private String accessCode;
    private UUID teacherId;
    private String teacherName;
    private LocalDateTime createdAt;

    public static GrupoResponse desde(Grupo g) {
        GrupoResponse r = new GrupoResponse();
        r.id          = g.getId();
        r.name        = g.getName();
        r.description = g.getDescription();
        r.accessCode  = g.getAccessCode();
        r.teacherId   = g.getTeacher().getId();
        r.teacherName = g.getTeacher().getName();
        r.createdAt   = g.getCreatedAt();
        return r;
    }
}
