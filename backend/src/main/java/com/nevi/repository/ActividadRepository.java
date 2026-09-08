package com.nevi.repository;

import com.nevi.entity.Actividad;
import com.nevi.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActividadRepository extends JpaRepository<Actividad, UUID> {
    // Actividades de un grupo, las más recientes primero.
    List<Actividad> findByGrupoOrderByCreatedAtDesc(Grupo grupo);
}
