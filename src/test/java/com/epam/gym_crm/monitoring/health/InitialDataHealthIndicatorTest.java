package com.epam.gym_crm.monitoring.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.dao.DataAccessResourceFailureException;

import com.epam.gym_crm.db.repository.TrainingTypeRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialDataHealthIndicatorTest {

	@Mock
	private TrainingTypeRepository mockTrainingTypeRepository;

	private InitialDataHealthIndicator initialDataHealthIndicator;

	@BeforeEach
	void setUp() {
		initialDataHealthIndicator = new InitialDataHealthIndicator(mockTrainingTypeRepository);
	}

	// --- Success ---
	@Test
	void testHealth_whenDataIsAvailable_shouldReturnUp() {

		when(mockTrainingTypeRepository.count()).thenReturn(5L);

		Health health = initialDataHealthIndicator.health();

		assertNotNull(health);
		assertEquals(Status.UP, health.getStatus(), "Health status should be UP when initial data is present");
		assertEquals(5L, health.getDetails().get("found_training_types"));
		assertEquals("Initial data is available.", health.getDetails().get("status"));

		verify(mockTrainingTypeRepository, times(1)).count();
	}

	// --- Failure 1 ---
	@Test
	void testHealth_whenDataIsMissing_shouldReturnDown() {
		when(mockTrainingTypeRepository.count()).thenReturn(0L);

		Health health = initialDataHealthIndicator.health();

		assertNotNull(health);
		assertEquals(Status.DOWN, health.getStatus(), "Health status should be DOWN when initial data is missing");
		assertEquals(0L, health.getDetails().get("found_training_types"));
		assertTrue(health.getDetails().get("error").toString().contains("Critical initial data"));
	}

	// --- Failure 2 ---
	@Test
	void testHealth_whenRepositoryThrowsException_shouldReturnDown() {
		// Arrange
		DataAccessResourceFailureException testException = new DataAccessResourceFailureException(
				"Cannot connect to DB");
		when(mockTrainingTypeRepository.count()).thenThrow(testException);

		// Act
		Health health = initialDataHealthIndicator.health();

		// Assert
		assertNotNull(health);
		assertEquals(Status.DOWN, health.getStatus());

		assertTrue(health.getDetails().containsKey("error"),
				"Details should contain the standard 'error' key from the exception");

		assertTrue(health.getDetails().containsKey("message"), "Details should contain our custom 'message' key");
		assertEquals("Failed to check training types data.", health.getDetails().get("message"));

		String errorString = (String) health.getDetails().get("error");
		assertTrue(errorString.contains("DataAccessResourceFailureException"),
				"Error string should contain the exception type");
	}
}