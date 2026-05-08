package br.com.todoapp.logging;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;

import java.io.IOException;
import java.util.UUID;

@Provider
@Priority(1)
public class CorrelationIdFilter implements ContainerRequestFilter {

    private static final String CORRELATION_ID = "correlationId";
    private static final String HEADER_NAME = "X-Correlation-ID";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String correlationId = requestContext.getHeaderString(HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CORRELATION_ID, correlationId);
        requestContext.getHeaders().putSingle(HEADER_NAME, correlationId);
    }
}
