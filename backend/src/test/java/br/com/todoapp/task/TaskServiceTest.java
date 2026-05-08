package br.com.todoapp.task;

import br.com.todoapp.security.UserContext;
import br.com.todoapp.util.SqlServerTestResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(SqlServerTestResource.class)
class TaskServiceTest {

    @Inject
    TaskService taskService;

    @InjectMock
    UserContext userContext;

    @BeforeEach
    void setup() {
        Mockito.when(userContext.getUserId()).thenReturn("user-1");
    }

    @Test
    void create_forcesStatusToCriada() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.title = "Test Task";
        req.priority = TaskPriority.ALTA;

        Task task = taskService.create(req);

        assertThat(task.status).isEqualTo(TaskStatus.CRIADA);
    }

    @Test
    void create_setsCreatedAtAndNullCompletedAt() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.title = "Test Task";

        Task task = taskService.create(req);

        assertThat(task.createdAt).isNotNull();
        assertThat(task.completedAt).isNull();
    }

    @Test
    void create_setsUserIdFromContext() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.title = "Test Task";

        Task task = taskService.create(req);

        assertThat(task.userId).isEqualTo("user-1");
    }

    @Test
    void create_defaultsPriorityToMedia_whenNotProvided() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.title = "Test Task";
        req.priority = null;

        Task task = taskService.create(req);

        assertThat(task.priority).isEqualTo(TaskPriority.MEDIA);
    }

    @Test
    void create_acceptsPastDueDate() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.title = "Retroactive Task";
        req.dueDate = LocalDate.of(2020, 1, 1);

        Task task = taskService.create(req);

        assertThat(task.dueDate).isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    void updateStatus_toConcluida_setsCompletedAt() {
        Task created = createTask("Task to complete");
        UpdateStatusRequest req = new UpdateStatusRequest();
        req.status = TaskStatus.CONCLUIDA;

        Task updated = taskService.updateStatus(created.id, req);

        assertThat(updated.status).isEqualTo(TaskStatus.CONCLUIDA);
        assertThat(updated.completedAt).isNotNull();
    }

    @Test
    void updateStatus_fromConcluida_toAndamento_clearsCompletedAt() {
        Task created = createTask("Task to toggle");
        UpdateStatusRequest toComplete = new UpdateStatusRequest();
        toComplete.status = TaskStatus.CONCLUIDA;
        taskService.updateStatus(created.id, toComplete);

        UpdateStatusRequest toAndamento = new UpdateStatusRequest();
        toAndamento.status = TaskStatus.ANDAMENTO;
        Task updated = taskService.updateStatus(created.id, toAndamento);

        assertThat(updated.status).isEqualTo(TaskStatus.ANDAMENTO);
        assertThat(updated.completedAt).isNull();
    }

    @Test
    void updateStatus_toAndamento_doesNotSetCompletedAt() {
        Task created = createTask("Task in progress");
        UpdateStatusRequest req = new UpdateStatusRequest();
        req.status = TaskStatus.ANDAMENTO;

        Task updated = taskService.updateStatus(created.id, req);

        assertThat(updated.completedAt).isNull();
    }

    @Test
    void delete_throwsNotFound_whenWrongOwner() {
        Task created = createTask("Owner task");
        Mockito.when(userContext.getUserId()).thenReturn("other-user");

        assertThatThrownBy(() -> taskService.delete(created.id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_throwsNotFound_whenWrongOwner() {
        Task created = createTask("Private task");
        Mockito.when(userContext.getUserId()).thenReturn("other-user");

        assertThatThrownBy(() -> taskService.findById(created.id))
                .isInstanceOf(NotFoundException.class);
    }

    private Task createTask(String title) {
        Mockito.when(userContext.getUserId()).thenReturn("user-1");
        CreateTaskRequest req = new CreateTaskRequest();
        req.title = title;
        return taskService.create(req);
    }
}
