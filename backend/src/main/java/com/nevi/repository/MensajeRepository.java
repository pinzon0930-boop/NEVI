package com.nevi.repository;

import com.nevi.entity.Grupo;
import com.nevi.entity.Mensaje;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MensajeRepository extends JpaRepository<Mensaje, UUID> {

    // Últimos N mensajes de un grupo, ordenados del más antiguo al más reciente.
    // Se usa una subconsulta para tomar los últimos y luego reordenarlos ascendente.
    @Query("""
        SELECT m FROM Mensaje m
        WHERE m.grupo = :grupo
        ORDER BY m.createdAt DESC
    """)
    List<Mensaje> findTopByGrupo(@Param("grupo") Grupo grupo, Pageable pageable);
}
