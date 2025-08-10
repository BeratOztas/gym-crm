package com.epam.gym_crm.monitoring.metric;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class AppMetrics {

	private final Counter traineeCreationCounter;
	private final Counter trainerCreationCounter;
	private final Counter trainingCreationCounter;
	private final Counter loginSuccessCounter;
	private final Counter loginFailureCounter;

	public AppMetrics(MeterRegistry meterRegistry) {
		
		this.traineeCreationCounter = meterRegistry.counter("gym_crm_creations_total", "type", "trainee");
		this.trainerCreationCounter = meterRegistry.counter("gym_crm_creations_total", "type", "trainer");
		this.trainingCreationCounter = meterRegistry.counter("gym_crm_creations_total", "type", "training");

		this.loginSuccessCounter = meterRegistry.counter("gym_crm_login_attempts_total", "status", "success");
		this.loginFailureCounter = meterRegistry.counter("gym_crm_login_attempts_total", "status", "failure");

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

	public void incrementLoginSuccess() {
		loginSuccessCounter.increment();
	}

	public void incrementLoginFailure() {
		loginFailureCounter.increment();
	}

}
