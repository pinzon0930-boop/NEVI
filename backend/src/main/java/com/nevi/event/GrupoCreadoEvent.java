package com.nevi.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record GrupoCreadoEvent(
        UUID grupoId,
        String nombre,
        UUID profesorId,
        LocalDateTime fechaCreacion
) {}