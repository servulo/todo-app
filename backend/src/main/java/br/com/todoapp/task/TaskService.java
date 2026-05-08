package br.com.todoapp.task;

import br.com.todoapp.security.UserContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class TaskService {

    @Inject
    UserContext userContext;

    @Transactional
    public Task create(CreateTaskRequest request) {
        Task task = new Task();
        task.title = request.title.strip();
        task.description = request.description;
        task.priority = request.priority != null ? request.priority : TaskPriority.MEDIA;
        task.status = TaskStatus.CRIADA;
        task.dueDate = request.dueDate;
        task.createdAt = LocalDateTime.now();
        task.completedAt = null;
        task.userId = userContext.getUserId();
        task.persist();
        return task;
    }

    @Transactional
    public Task update(UUID id, UpdateTaskRequest request) {
        Task task = findOwned(id);
        task.title = request.title.strip();
        task.description = request.description;
        task.priority = request.priority;
        task.dueDate = request.dueDate;
        return task;
    }

    @Transactional
    public Task updateStatus(UUID id, UpdateStatusRequest request) {
        Task task = findOwned(id);
        TaskStatus previous = task.status;
        task.status = request.status;

        if (request.status == TaskStatus.CONCLUIDA) {
            task.completedAt = LocalDateTime.now();
        } else if (previous == TaskStatus.CONCLUIDA) {
            task.completedAt = null;
        }

        return task;
    }

    @Transactional
    public void delete(UUID id) {
        Task task = findOwned(id);
        task.delete();
    }

    public Task findById(UUID id) {
        return findOwned(id);
    }

    private Task findOwned(UUID id) {
        return Task.findByIdAndUserId(id, userContext.getUserId())
                .orElseThrow(NotFoundException::new);
    }
}
