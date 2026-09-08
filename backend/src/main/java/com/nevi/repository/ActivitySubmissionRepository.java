package com.nevi.repository;

import com.nevi.entity.ActivitySubmission;
import com.nevi.entity.Actividad;
import com.nevi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ActivitySubmissionRepository extends JpaRepository<ActivitySubmission, UUID> {

    boolean existsByActividadAndStudent(Actividad actividad, User student);

    // IDs de actividades entregadas por un estudiante.
    @Query("SELECT s.actividad.id FROM ActivitySubmission s WHERE s.student = :student")
    List<UUID> findActividadIdsByStudent(@Param("student") User student);
}
