package com.nevi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

// Tabla "activity_submissions" — registra cuándo un estudiante entregó una actividad.
@Entity
@Table(
    name = "activity_submissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "student_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivitySubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;
}
