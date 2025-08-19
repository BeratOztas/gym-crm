package com.epam.gym_crm.monitoring.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        
        appMetrics.incrementTrainerCreation();

        Counter counter = meterRegistry.get("gym_crm_creations_total").tag("type", "trainer").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count(), "Trainer creation counter should be incremented to 1.");
    }

    @Test
    void testTrainingCreationCounter_incrementsCorrectly() {
    	
        appMetrics.incrementTrainingCreation();

        
        Counter counter = meterRegistry.get("gym_crm_creations_total").tag("type", "training").counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count(), "Training creation counter should be incremented to 1.");
    }

    @Test
    void testLoginSuccessCounter_incrementsCorrectly() {
        String username = "testuser_success";

        appMetrics.incrementLoginSuccess(username);

        Counter counter = meterRegistry.get("gym_crm_login_attempts_total")
                .tag("status", "success")
                .tag("username", username)
                .counter();
        assertNotNull(counter);
        assertEquals(1.0, counter.count(), "Login success counter for a specific user should be incremented to 1.");
    }

    @Test
    void testLoginFailureCounter_incrementsCorrectly() {
        String username = "testuser_failure";
        
        appMetrics.incrementLoginFailure(username);
        appMetrics.incrementLoginFailure(username);
        appMetrics.incrementLoginFailure(username);

        Counter counter = meterRegistry.get("gym_crm_login_attempts_total")
                .tag("status", "failure")
                .tag("username", username)
                .counter();
        assertNotNull(counter);
        assertEquals(3.0, counter.count(), "Login failure counter for a specific user should be incremented to 3.");
    }

    @Test
    void testCounters_areIndependent() {
    	
        appMetrics.incrementTraineeCreation();
        appMetrics.incrementLoginFailure("user1");

        Counter traineeCounter = meterRegistry.get("gym_crm_creations_total").tag("type", "trainee").counter();
        Counter trainerCounter = meterRegistry.get("gym_crm_creations_total").tag("type", "trainer").counter();
        Counter loginFailureCounter = meterRegistry.get("gym_crm_login_attempts_total").tag("status", "failure").tag("username", "user1").counter();
        
        assertThrows(io.micrometer.core.instrument.search.MeterNotFoundException.class, () -> {
            meterRegistry.get("gym_crm_login_attempts_total").tag("status", "success").counter();
        }, "Login success counter should not exist and throw an exception.");
        

        assertEquals(1.0, traineeCounter.count());
        assertEquals(0.0, trainerCounter.count(), "Trainer counter should remain 0.");
        assertEquals(1.0, loginFailureCounter.count());
    }

    @Test
    void testLoginCounters_withDifferentUsers_areIndependent() {
    	
        appMetrics.incrementLoginSuccess("userA");
        appMetrics.incrementLoginFailure("userB");

        Counter userASuccessCounter = meterRegistry.get("gym_crm_login_attempts_total")
                .tag("status", "success")
                .tag("username", "userA")
                .counter();
        assertNotNull(userASuccessCounter);
        assertEquals(1.0, userASuccessCounter.count());

        Counter userBSuccessCounter = meterRegistry.find("gym_crm_login_attempts_total")
                .tag("status", "success")
                .tag("username", "userB")
                .counter();
        assertNull(userBSuccessCounter, "User B's success counter should not exist.");

        Counter userBFailureCounter = meterRegistry.get("gym_crm_login_attempts_total")
                .tag("status", "failure")
                .tag("username", "userB")
                .counter();
        assertNotNull(userBFailureCounter);
        assertEquals(1.0, userBFailureCounter.count());
    }
}