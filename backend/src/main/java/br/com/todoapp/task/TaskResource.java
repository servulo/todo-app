package br.com.todoapp.task;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Path("/api/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Tasks", description = "Task management operations")
public class TaskResource {

    @Inject
    TaskService taskService;

    @POST
    @RolesAllowed("**")
    @Operation(summary = "Create a new task")
    public Response create(@Valid CreateTaskRequest request) {
        Task task = taskService.create(request);
        return Response.status(Response.Status.CREATED)
                .entity(TaskResponse.from(task))
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("**")
    @Operation(summary = "Update task fields (non-status)")
    public TaskResponse update(@PathParam("id") UUID id, @Valid UpdateTaskRequest request) {
        return TaskResponse.from(taskService.update(id, request));
    }

    @PATCH
    @Path("/{id}/status")
    @RolesAllowed("**")
    @Operation(summary = "Update task status")
    public TaskResponse updateStatus(@PathParam("id") UUID id, @Valid UpdateStatusRequest request) {
        return TaskResponse.from(taskService.updateStatus(id, request));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("**")
    @Operation(summary = "Permanently delete a task")
    public Response delete(@PathParam("id") UUID id) {
        taskService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("**")
    @Operation(summary = "Get task details")
    public TaskResponse getById(@PathParam("id") UUID id) {
        return TaskResponse.from(taskService.findById(id));
    }
}
