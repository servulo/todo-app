package br.com.todoapp.task;

import br.com.todoapp.security.UserContext;
import br.com.todoapp.util.SqlServerTestResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@QuarkusTestResource(SqlServerTestResource.class)
class TaskResourceContractTest {

    @Inject
    TaskService taskService;

    @InjectMock
    UserContext userContext;

    // --- POST /api/tasks ---

    @Test
    @TestSecurity(user = "user-a")
    void post_returns201_onValidRequest() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"Test Task\",\"priority\":\"ALTA\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("status", equalTo("CRIADA"))
            .body("createdAt", notNullValue())
            .body("completedAt", nullValue());
    }

    @Test
    @TestSecurity(user = "user-a")
    void post_returns400_onBlankTitle() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"   \"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(400)
            .body("errors", not(empty()));
    }

    @Test
    @TestSecurity(user = "user-a")
    void post_returns400_onMissingTitle() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"priority\":\"ALTA\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(400);
    }

    @Test
    @TestSecurity(user = "user-a")
    void post_returns400_onInvalidPriority() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"Task\",\"priority\":\"URGENTE\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(400);
    }

    @Test
    void post_returns401_whenNoJwt() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"Task\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user-a")
    void post_returns201_whenDueDateIsInPast() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"Retroactive Task\",\"dueDate\":\"2020-01-01\"}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(201)
            .body("dueDate", equalTo("2020-01-01"));
    }

    // --- PATCH /api/tasks/{id}/status ---

    @Test
    @TestSecurity(user = "user-a")
    void patch_status_returns200_onValidTransition() {
        UUID id = createTaskDirect("user-a", "Status Task");

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
    @TestSecurity(user = "user-a")
    void patch_status_returns400_onInvalidStatus() {
        UUID id = createTaskDirect("user-a", "Invalid Status Task");

        given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"INVALID\"}")
        .when()
            .patch("/api/tasks/{id}/status", id)
        .then()
            .statusCode(400);
    }

    @Test
    void patch_status_returns401_whenNoJwt() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"ANDAMENTO\"}")
        .when()
            .patch("/api/tasks/{id}/status", UUID.randomUUID())
        .then()
            .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user-b")
    void patch_status_returns404_onWrongOwner() {
        UUID id = createTaskDirect("user-a", "Other User Task for PATCH");

        given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"ANDAMENTO\"}")
        .when()
            .patch("/api/tasks/{id}/status", id)
        .then()
            .statusCode(404);
    }

    // --- PUT /api/tasks/{id} ---

    @Test
    @TestSecurity(user = "user-a")
    void put_returns200_onValidRequest() {
        UUID id = createTaskDirect("user-a", "Update Task");

        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"Updated Title\",\"priority\":\"MEDIA\"}")
        .when()
            .put("/api/tasks/{id}", id)
        .then()
            .statusCode(200)
            .body("title", equalTo("Updated Title"));
    }

    @Test
    @TestSecurity(user = "user-a")
    void put_returns400_onBlankTitle() {
        UUID id = createTaskDirect("user-a", "Put Validation Task");

        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"\",\"priority\":\"MEDIA\"}")
        .when()
            .put("/api/tasks/{id}", id)
        .then()
            .statusCode(400);
    }

    @Test
    @TestSecurity(user = "user-b")
    void put_returns404_onWrongOwner() {
        UUID id = createTaskDirect("user-a", "Owner Put Task");

        given()
            .contentType(ContentType.JSON)
            .body("{\"title\":\"Hack\",\"priority\":\"MEDIA\"}")
        .when()
            .put("/api/tasks/{id}", id)
        .then()
            .statusCode(404);
    }

    // --- DELETE /api/tasks/{id} ---

    @Test
    @TestSecurity(user = "user-a")
    void delete_returns204_onSuccess() {
        UUID id = createTaskDirect("user-a", "Delete Task");

        given()
        .when()
            .delete("/api/tasks/{id}", id)
        .then()
            .statusCode(204);
    }

    @Test
    @TestSecurity(user = "user-a")
    void delete_returns404_onNonExistentId() {
        given()
        .when()
            .delete("/api/tasks/{id}", UUID.randomUUID())
        .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "user-b")
    void delete_returns404_onWrongOwner() {
        UUID id = createTaskDirect("user-a", "Owner Delete Task");

        given()
        .when()
            .delete("/api/tasks/{id}", id)
        .then()
            .statusCode(404);
    }

    @Test
    void delete_returns401_whenNoJwt() {
        given()
        .when()
            .delete("/api/tasks/{id}", UUID.randomUUID())
        .then()
            .statusCode(401);
    }

    // --- GET /api/tasks/{id} ---

    @Test
    @TestSecurity(user = "user-a")
    void get_returns200_withAllFields() {
        UUID id = createTaskDirect("user-a", "Get Task");

        given()
        .when()
            .get("/api/tasks/{id}", id)
        .then()
            .statusCode(200)
            .body("id", equalTo(id.toString()))
            .body("title", notNullValue())
            .body("priority", notNullValue())
            .body("status", notNullValue())
            .body("createdAt", notNullValue());
    }

    @Test
    @TestSecurity(user = "user-a")
    void get_returns404_onNonExistent() {
        given()
        .when()
            .get("/api/tasks/{id}", UUID.randomUUID())
        .then()
            .statusCode(404);
    }

    @Test
    @TestSecurity(user = "user-b")
    void get_returns404_onWrongOwner() {
        UUID id = createTaskDirect("user-a", "Private Task");

        given()
        .when()
            .get("/api/tasks/{id}", id)
        .then()
            .statusCode(404);
    }

    @Test
    void get_returns401_whenNoJwt() {
        given()
        .when()
            .get("/api/tasks/{id}", UUID.randomUUID())
        .then()
            .statusCode(401);
    }

    private UUID createTaskDirect(String userId, String title) {
        Mockito.when(userContext.getUserId()).thenReturn(userId);
        CreateTaskRequest req = new CreateTaskRequest();
        req.title = title;
        return taskService.create(req).id;
    }
}
