package com.nevi.service;

import com.nevi.dto.GrupoRequest;
import com.nevi.dto.GrupoResponse;
import com.nevi.entity.GroupMember;
import com.nevi.entity.Grupo;
import com.nevi.entity.User;
import com.nevi.repository.GroupMemberRepository;
import com.nevi.repository.GrupoRepository;
import com.nevi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final GroupMemberRepository memberRepository;
    private final UserRepository userRepository;

    // Crea un nuevo grupo. Solo los profesores llegan a este método (se valida en el controlador).
    @Transactional
    public GrupoResponse crear(GrupoRequest req, String emailProfesor) {
        User profesor = obtenerUsuario(emailProfesor);

        Grupo grupo = Grupo.builder()
            .name(req.getNombre())
            .description(req.getDescripcion())
            .teacher(profesor)
            .accessCode(generarCodigo())
            .build();

        return GrupoResponse.desde(grupoRepository.save(grupo));
    }

    // Devuelve los grupos del usuario según su rol.
    @Transactional(readOnly = true)
    public List<GrupoResponse> misGrupos(String email) {
        User user = obtenerUsuario(email);

        if ("teacher".equals(user.getRole())) {
            return grupoRepository
                .findByTeacherOrderByCreatedAtDesc(user)
                .stream()
                .map(GrupoResponse::desde)
                .toList();
        }

        // Estudiante: grupos a través de la tabla group_members.
        return memberRepository.findByStudent(user)
            .stream()
            .map(gm -> GrupoResponse.desde(gm.getGrupo()))
            .toList();
    }

    // Permite a un estudiante unirse a un grupo con el código de acceso.
    @Transactional
    public GrupoResponse unirse(String codigo, String emailEstudiante) {
        User estudiante = obtenerUsuario(emailEstudiante);

        Grupo grupo = grupoRepository.findByAccessCode(codigo.toUpperCase())
            .orElseThrow(() -> new IllegalArgumentException("Código de acceso inválido."));

        if (memberRepository.existsByGrupoAndStudent(grupo, estudiante)) {
            throw new IllegalArgumentException("Ya eres miembro de este grupo.");
        }

        GroupMember member = GroupMember.builder()
            .grupo(grupo)
            .student(estudiante)
            .build();

        memberRepository.save(member);
        return GrupoResponse.desde(grupo);
    }

    // Obtiene un grupo por ID y lo convierte a DTO.
    @Transactional(readOnly = true)
    public GrupoResponse obtenerPorId(String grupoId) {
        Grupo grupo = grupoRepository.findById(java.util.UUID.fromString(grupoId))
            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado."));
        return GrupoResponse.desde(grupo);
    }

    // ── Utilidades privadas ──────────────────────────────────────────────────

    private User obtenerUsuario(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
    }

    // Genera un código de 6 caracteres sin letras confusas (O, 0, I, l).
    private String generarCodigo() {
        String caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder codigo = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            codigo.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return codigo.toString();
    }
}
