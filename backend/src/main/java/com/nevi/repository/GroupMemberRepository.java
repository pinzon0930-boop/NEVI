package com.nevi.repository;

import com.nevi.entity.GroupMember;
import com.nevi.entity.Grupo;
import com.nevi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    // Verifica si un estudiante ya es miembro de un grupo.
    boolean existsByGrupoAndStudent(Grupo grupo, User student);

    // Obtiene todos los grupos a los que pertenece un estudiante.
    @Query("SELECT gm FROM GroupMember gm JOIN FETCH gm.grupo g ORDER BY g.createdAt DESC")
    List<GroupMember> findByStudentOrderByGrupoCreatedAtDesc(@Param("student") User student);

    // Versión sin JOIN FETCH para consultas simples.
    List<GroupMember> findByStudent(User student);
}
