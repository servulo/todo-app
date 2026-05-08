package br.com.todoapp.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateTaskRequest {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 255)
    public String title;

    public String description;

    @NotNull(message = "A prioridade é obrigatória")
    public TaskPriority priority;

    public LocalDate dueDate;
}
