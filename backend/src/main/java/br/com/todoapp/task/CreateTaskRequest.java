package br.com.todoapp.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateTaskRequest {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 255)
    public String title;

    public String description;

    public TaskPriority priority = TaskPriority.MEDIA;

    public LocalDate dueDate;
}
