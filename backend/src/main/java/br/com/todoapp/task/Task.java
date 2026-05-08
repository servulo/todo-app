package br.com.todoapp.task;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "task")
public class Task extends PanacheEntityBase {

    @Id
    public UUID id = UUID.randomUUID();

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    public String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    public String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public TaskPriority priority = TaskPriority.MEDIA;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public TaskStatus status = TaskStatus.CRIADA;

    @Column(name = "due_date")
    public LocalDate dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @NotBlank
    @Column(name = "user_id", nullable = false, length = 255)
    public String userId;

    public static Optional<Task> findByIdAndUserId(UUID id, String userId) {
        return find("id = ?1 and userId = ?2", id, userId).firstResultOptional();
    }
}
