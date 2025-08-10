package com.epam.gym_crm.monitoring.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppMetricsTest {

    private MeterRegistry meterRegistry;
    private AppMetrics appMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        
        appMetrics = new AppMetrics(meterRegistry);
    }

    @Test
    void testTraineeCreationCounter_incrementsCorrectly() {

        appMetrics.incrementTraineeCreation();
        appMetrics.incrementTraineeCreation(); 

        Counter counter = meterRegistry.get("gym_crm_creations_total").tag("type", "trainee").counter();
        assertNotNull(counter);
        assertEquals(2.0, counter.count(), "Trainee creation counter should be incremented to 2.");
    }

    @Test
    void testTrainerCreationCounter_incrementsCorrectly() {
        // Arrange
        
        // Act
        appMetrics.incrementTrainerCreation();

        // Assert
        Counter counter = meterRegistry.get("gym_crm_creations_total").tag("type", "trainer").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count(), "Trainer creation counter should be incremented to 1.");
    }

    @Test
    void testTrainingCreationCounter_incrementsCorrectly() {
        // Act
        appMetrics.incrementTrainingCreation();

        // Assert
        Counter counter = meterRegistry.get("gym_crm_creations_total").tag("type", "training").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count(), "Training creation counter should be incremented to 1.");
    }

    @Test
    void testLoginSuccessCounter_incrementsCorrectly() {
        // Act
        appMetrics.incrementLoginSuccess();

        // Assert
        Counter counter = meterRegistry.get("gym_crm_login_attempts_total").tag("status", "success").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count(), "Login success counter should be incremented to 1.");
    }

    @Test
    void testLoginFailureCounter_incrementsCorrectly() {
        // Act
        appMetrics.incrementLoginFailure();
        appMetrics.incrementLoginFailure();
        appMetrics.incrementLoginFailure(); 

        // Assert
        Counter counter = meterRegistry.get("gym_crm_login_attempts_total").tag("status", "failure").counter();
        assertNotNull(counter);
        assertEquals(3.0, counter.count(), "Login failure counter should be incremented to 3.");
    }

    @Test
    void testCounters_areIndependent() {
        
        // Act
        appMetrics.incrementTraineeCreation();
        appMetrics.incrementLoginFailure();

        // Assert
        Counter traineeCounter = meterRegistry.get("gym_crm_creations_total").tag("type", "trainee").counter();
        Counter trainerCounter = meterRegistry.get("gym_crm_creations_total").tag("type", "trainer").counter();
        Counter loginFailureCounter = meterRegistry.get("gym_crm_login_attempts_total").tag("status", "failure").counter();
        Counter loginSuccessCounter = meterRegistry.get("gym_crm_login_attempts_total").tag("status", "success").counter();

        assertEquals(1.0, traineeCounter.count());
        assertEquals(0.0, trainerCounter.count(), "Trainer counter should remain 0.");
        assertEquals(1.0, loginFailureCounter.count());
        assertEquals(0.0, loginSuccessCounter.count(), "Login success counter should remain 0.");
    }
}