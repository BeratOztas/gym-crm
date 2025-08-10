package com.epam.gym_crm.monitoring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                return Health.up().withDetail("database", "Service is available").build();
            } else {
                return Health.down().withDetail("database", "Connection is not valid").build();
            }
        } catch (SQLException e) {
            return Health.down(e).withDetail("database", "Failed to get connection").build();
        }
    }
}