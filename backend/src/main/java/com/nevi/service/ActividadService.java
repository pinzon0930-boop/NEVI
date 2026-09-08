package com.nevi.service;

import com.nevi.dto.ActividadRequest;
import com.nevi.dto.ActividadResponse;
import com.nevi.entity.Actividad;
import com.nevi.entity.ActivitySubmission;
import com.nevi.entity.Grupo;
import com.nevi.entity.User;
import com.nevi.repository.ActividadRepository;
import com.nevi.repository.ActivitySubmissionRepository;
import com.nevi.repository.GrupoRepository;
import com.nevi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActividadService {

    private final ActividadRepository actividadRepository;
    private final ActivitySubmissionRepository submissionRepository;
    private final GrupoRepository grupoRepository;
    private final UserRepository userRepository;

    // Lista todas las actividades de un grupo, ordenadas por fecha de creación descendente.
    @Transactional(readOnly = true)
    public List<ActividadResponse> obtener(UUID grupoId) {
        Grupo grupo = obtenerGrupo(grupoId);
        return actividadRepository.findByGrupoOrderByCreatedAtDesc(grupo)
            .stream()
            .map(ActividadResponse::desde)
            .toList();
    }

    // Crea una nueva actividad en un grupo.
    @Transactional
    public ActividadResponse crear(ActividadRequest req, String emailProfesor) {
        User profesor = obtenerUsuario(emailProfesor);
        Grupo grupo   = obtenerGrupo(req.getGrupoId());

        LocalDateTime fechaEntrega = null;
        if (req.getFechaEntrega() != null && !req.getFechaEntrega().isBlank()) {
            fechaEntrega = LocalDateTime.parse(req.getFechaEntrega());
        }

        Actividad actividad = Actividad.builder()
            .grupo(grupo)
            .title(req.getTitulo())
            .description(req.getDescripcion())
            .dueDate(fechaEntrega)
            .createdBy(profesor)
            .build();

        return ActividadResponse.desde(actividadRepository.save(actividad));
    }

    // Registra la entrega de un estudiante para una actividad.
    @Transactional
    public void entregar(UUID actividadId, String emailEstudiante) {
        User estudiante = obtenerUsuario(emailEstudiante);
        Actividad actividad = actividadRepository.findById(actividadId)
            .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada."));

        if (submissionRepository.existsByActividadAndStudent(actividad, estudiante)) {
            throw new IllegalArgumentException("Ya entregaste esta actividad.");
        }

        ActivitySubmission entrega = ActivitySubmission.builder()
            .actividad(actividad)
            .student(estudiante)
            .build();

        submissionRepository.save(entrega);
    }

    // Devuelve los IDs de actividades ya entregadas por el estudiante.
    @Transactional(readOnly = true)
    public List<UUID> misEntregas(String emailEstudiante) {
        User estudiante = obtenerUsuario(emailEstudiante);
        return submissionRepository.findActividadIdsByStudent(estudiante);
    }

    // ── Utilidades privadas ──────────────────────────────────────────────────

    private User obtenerUsuario(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    private Grupo obtenerGrupo(UUID id) {
        return grupoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado."));
    }
}
