package br.com.todoapp.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class SqlServerTestResource implements QuarkusTestResourceLifecycleManager {

    private static final DockerImageName IMAGE =
            DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-latest")
                    .asCompatibleSubstituteFor("mcr.microsoft.com/mssql/server");

    private MSSQLServerContainer<?> container;

    @Override
    public Map<String, String> start() {
        container = new MSSQLServerContainer<>(IMAGE).acceptLicense();
        container.start();
        return Map.of(
                "quarkus.datasource.jdbc.url", container.getJdbcUrl() + ";encrypt=false",
                "quarkus.datasource.username", container.getUsername(),
                "quarkus.datasource.password", container.getPassword()
        );
    }

    @Override
    public void stop() {
        if (container != null && container.isRunning()) {
            container.stop();
        }
    }
}
