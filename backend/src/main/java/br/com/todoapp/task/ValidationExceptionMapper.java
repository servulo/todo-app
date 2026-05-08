package br.com.todoapp.task;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<Map<String, String>> errors = exception.getConstraintViolations().stream()
                .map(cv -> Map.of(
                        "field", extractField(cv.getPropertyPath().toString()),
                        "message", cv.getMessage()
                ))
                .collect(Collectors.toList());
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("errors", errors))
                .build();
    }

    private String extractField(String propertyPath) {
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }
}
