package br.com.todoapp.task;

import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "O status é obrigatório")
    public TaskStatus status;
}
