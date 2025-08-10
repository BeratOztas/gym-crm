package com.epam.gym_crm.monitoring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DiskSpaceHealthIndicator implements HealthIndicator {

    private static final long THRESHOLD_BYTES = 1 * 1024 * 1024 * 1024;
    private final File path = new File("."); 

    @Override
    public Health health() {
        long freeSpace = path.getFreeSpace();
        long totalSpace = path.getTotalSpace();

        if (freeSpace >= THRESHOLD_BYTES) {
            return Health.up()
                    .withDetail("total_space", totalSpace / (1024 * 1024) + " MB")
                    .withDetail("free_space", freeSpace / (1024 * 1024) + " MB")
                    .withDetail("threshold", THRESHOLD_BYTES / (1024 * 1024) + " MB")
                    .build();
        } else {
            return Health.down()
                    .withDetail("total_space", totalSpace / (1024 * 1024) + " MB")
                    .withDetail("free_space", freeSpace / (1024 * 1024) + " MB")
                    .withDetail("threshold", THRESHOLD_BYTES / (1024 * 1024) + " MB")
                    .withDetail("error", "Disk space is critically low!")
                    .build();
        }
    }
}