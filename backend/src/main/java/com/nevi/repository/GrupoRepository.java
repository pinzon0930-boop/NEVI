package com.nevi.repository;

import com.nevi.entity.Grupo;
import com.nevi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GrupoRepository extends JpaRepository<Grupo, UUID> {
    // Grupos creados por un profesor (ordenados por fecha descendente).
    List<Grupo> findByTeacherOrderByCreatedAtDesc(User teacher);
    // Buscar grupo por código de acceso.
    Optional<Grupo> findByAccessCode(String accessCode);
}
