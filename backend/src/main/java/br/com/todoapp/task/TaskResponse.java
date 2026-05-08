package br.com.todoapp.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TaskResponse {

    public UUID id;
    public String title;
    public String description;
    public TaskPriority priority;
    public TaskStatus status;
    public LocalDate dueDate;
    public LocalDateTime createdAt;
    public LocalDateTime completedAt;

    public static TaskResponse from(Task task) {
        TaskResponse r = new TaskResponse();
        r.id = task.id;
        r.title = task.title;
        r.description = task.description;
        r.priority = task.priority;
        r.status = task.status;
        r.dueDate = task.dueDate;
        r.createdAt = task.createdAt;
        r.completedAt = task.completedAt;
        return r;
    }
}
