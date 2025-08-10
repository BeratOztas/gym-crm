package com.epam.gym_crm.monitoring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.epam.gym_crm.db.repository.TrainingTypeRepository;

@Component("initialDataHealth")
public class InitialDataHealthIndicator implements HealthIndicator {

	private final TrainingTypeRepository trainingTypeRepository;
	private static final long MINIMUM_REQUIRED_TYPES = 1;

	public InitialDataHealthIndicator(TrainingTypeRepository trainingTypeRepository) {
		this.trainingTypeRepository = trainingTypeRepository;
	}

	@Override
	public Health health() {
		try {
			long count = trainingTypeRepository.count();
			if (count >= MINIMUM_REQUIRED_TYPES) {
				return Health.up().withDetail("found_training_types", count)
						.withDetail("status", "Initial data is available.").build();
			} else {
				return Health.down().withDetail("found_training_types", count)
						.withDetail("error", "Critical initial data (Training Types) is missing!").build();
			}
		} catch (Exception e) {
			return Health.down(e).withDetail("error", "Failed to check training types data.").build();
		}
	}
}