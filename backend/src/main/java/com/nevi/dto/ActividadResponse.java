package com.nevi.dto;

import com.nevi.entity.Actividad;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ActividadResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private UUID createdBy;
    private LocalDateTime createdAt;

    public static ActividadResponse desde(Actividad a) {
        ActividadResponse r = new ActividadResponse();
        r.id          = a.getId();
        r.title       = a.getTitle();
        r.description = a.getDescription();
        r.dueDate     = a.getDueDate();
        r.createdBy   = a.getCreatedBy().getId();
        r.createdAt   = a.getCreatedAt();
        return r;
    }
}
