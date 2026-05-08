package br.com.todoapp.task;

import br.com.todoapp.security.UserContext;
import br.com.todoapp.util.SqlServerTestResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(SqlServerTestResource.class)
class TaskResourceIT {

    @Inject
    TaskService taskService;

    @InjectMock
    UserContext userContext;

    @BeforeEach
    void setup() {
        Mockito.when(userContext.getUserId()).thenReturn("it-user-1");
    }

    @Test
    @TestSecurity(user = "it-user-1")
    void createTask_persistsWithCorrectDefaults() {
        ValidatableResponse response = given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"Persistent Task\",\"priority\":\"ALTA\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(201);

        String id = response.extract().path("id");
        assertThat(id).isNotNull();

        Task persisted = Task.findById(UUID.fromString(id));
        assertThat(persisted).isNotNull();
        assertThat(persisted.status).isEqualTo(TaskStatus.CRIADA);
        assertThat(persisted.createdAt).isNotNull();
        assertThat(persisted.completedAt).isNull();
        assertThat(persisted.priority).isEqualTo(TaskPriority.ALTA);
        assertThat(persisted.userId).isEqualTo("it-user-1");
    }

    @Test
    @TestSecurity(user = "it-user-1")
    void createTask_setsCreatedAtAndNullCompletedAt() {
        String id = createTaskHttp("it-user-1", "Timestamp Task");

        given()
        .when()
            .get("/api/tasks/{id}", id)
        .then()
            .statusCode(200)
            .body("createdAt", notNullValue())
            .body("completedAt", nullValue())
            .body("status", equalTo("CRIADA"));
    }

    @Test
    @TestSecurity(user = "it-user-1")
    void statusTransition_toConcluida_setsCompletedAt() {
        String id = createTaskHttp("it-user-1", "Transition Task");

        given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"CONCLUIDA\"}")
        .when()
            .patch("/api/tasks/{id}/status", id)
        .then()
            .statusCode(200)
            .body("status", equalTo("CONCLUIDA"))
            .body("completedAt", notNullValue());
    }

    @Test
    @TestSecurity(user = "it-user-1")
    void statusTransition_fromConcluida_toAndamento_clearsCompletedAt() {
        String id = createTaskHttp("it-user-1", "Toggle Task");

        given().contentType(ContentType.JSON).body("{\"status\":\"CONCLUIDA\"}")
            .patch("/api/tasks/{id}/status", id);

        given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"ANDAMENTO\"}")
        .when()
            .patch("/api/tasks/{id}/status", id)
        .then()
            .statusCode(200)
            .body("status", equalTo("ANDAMENTO"))
            .body("completedAt", nullValue());
    }

    @Test
    @TestSecurity(user = "it-user-1")
    void deleteTask_removesFromDatabase() {
        String id = createTaskHttp("it-user-1", "Delete IT Task");

        given().delete("/api/tasks/{id}", id).then().statusCode(204);

        given().get("/api/tasks/{id}", id).then().statusCode(404);

        Task deleted = Task.findById(UUID.fromString(id));
        assertThat(deleted).isNull();
    }

    @Test
    @TestSecurity(user = "it-user-2")
    void crossUser_cannotAccessAnotherUsersTask() {
        UUID id = createTaskDirect("it-user-1", "User1 Private Task");

        given()
        .when()
            .get("/api/tasks/{id}", id)
        .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "it-user-2")
    void crossUser_cannotDeleteAnotherUsersTask() {
        UUID id = createTaskDirect("it-user-1", "User1 Delete Target");

        given()
        .when()
            .delete("/api/tasks/{id}", id)
        .then()
            .statusCode(404);

        Mockito.when(userContext.getUserId()).thenReturn("it-user-1");
        assertThat(Task.findByIdAndUserId(id, "it-user-1")).isPresent();
    }

    @Test
    @TestSecurity(user = "it-user-1")
    void getAllFields_areReturnedInResponse() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"Full Task\",\"description\":\"Desc\",\"priority\":\"BAIXA\",\"dueDate\":\"2026-12-31\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(201);

        String id = given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"Fields Task\",\"description\":\"Test Desc\",\"priority\":\"ALTA\",\"dueDate\":\"2026-06-01\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(201)
            .extract().path("id");

        given()
        .when()
            .get("/api/tasks/{id}", id)
        .then()
            .statusCode(200)
            .body("title", equalTo("Fields Task"))
            .body("description", equalTo("Test Desc"))
            .body("priority", equalTo("ALTA"))
            .body("dueDate", equalTo("2026-06-01"))
            .body("status", equalTo("CRIADA"))
            .body("createdAt", notNullValue())
            .body("completedAt", nullValue());
    }

    @Test
    @TestSecurity(user = "it-user-1")
    void completedTask_showsCompletedAtInDetails() {
        String id = createTaskHttp("it-user-1", "Complete For Details");

        given().contentType(ContentType.JSON).body("{\"status\":\"CONCLUIDA\"}")
            .patch("/api/tasks/{id}/status", id);

        given()
        .when()
            .get("/api/tasks/{id}", id)
        .then()
            .statusCode(200)
            .body("completedAt", notNullValue())
            .body("status", equalTo("CONCLUIDA"));
    }

    private String createTaskHttp(String userId, String title) {
        Mockito.when(userContext.getUserId()).thenReturn(userId);
        return given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"" + title + "\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(201)
            .extract().path("id");
    }

    private UUID createTaskDirect(String userId, String title) {
        Mockito.when(userContext.getUserId()).thenReturn(userId);
        CreateTaskRequest req = new CreateTaskRequest();
        req.title = title;
        return taskService.create(req).id;
    }
}
