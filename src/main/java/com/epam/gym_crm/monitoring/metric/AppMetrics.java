package com.epam.gym_crm.monitoring.metric;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

@Component
public class AppMetrics {

	private final Counter traineeCreationCounter;
	private final Counter trainerCreationCounter;
	private final Counter trainingCreationCounter;
	private final MeterRegistry meterRegistry;

	public AppMetrics(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;

		this.traineeCreationCounter = meterRegistry.counter("gym_crm_creations_total", "type", "trainee");
		this.trainerCreationCounter = meterRegistry.counter("gym_crm_creations_total", "type", "trainer");
		this.trainingCreationCounter = meterRegistry.counter("gym_crm_creations_total", "type", "training");

	}

	// ------ Counter Increment Methods ------

	public void incrementTraineeCreation() {
		traineeCreationCounter.increment();
	}

	public void incrementTrainerCreation() {
		trainerCreationCounter.increment();
	}

	public void incrementTrainingCreation() {
		trainingCreationCounter.increment();
	}

	public void incrementLoginSuccess(String username) {
		meterRegistry.counter("gym_crm_login_attempts_total", Tags.of("status", "success", "username", username))
				.increment();
	}

	public void incrementLoginFailure(String username) {
		meterRegistry.counter("gym_crm_login_attempts_total", Tags.of("status", "failure", "username", username))
				.increment();
	}

}
