package com.epam.gym_crm.monitoring.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseHealthIndicatorTest {

    @Mock
    private DataSource mockDataSource; 

    @Mock
    private Connection mockConnection; 

    private DatabaseHealthIndicator databaseHealthIndicator;

    @BeforeEach
    void setUp() {
        databaseHealthIndicator = new DatabaseHealthIndicator(mockDataSource);
    }

    // --- Success ---
    @Test
    void testHealth_whenConnectionIsValid_shouldReturnUp() throws SQLException {
    	
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        
        when(mockConnection.isValid(anyInt())).thenReturn(true);

        Health health = databaseHealthIndicator.health();

        assertNotNull(health);
        assertEquals(Status.UP, health.getStatus(), "Health status should be UP when connection is valid");
        assertTrue(health.getDetails().containsKey("database"));
        assertEquals("Service is available", health.getDetails().get("database"));

        verify(mockDataSource, times(1)).getConnection();
        verify(mockConnection, times(1)).isValid(2);
    }

    // --- Failure Scenario 1 ---
    @Test
    void testHealth_whenConnectionIsInvalid_shouldReturnDown() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(anyInt())).thenReturn(false);

        // Act
        Health health = databaseHealthIndicator.health();

        // Assert
        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus(), "Health status should be DOWN when connection is invalid");
        assertEquals("Connection is not valid", health.getDetails().get("database"));
    }

    // --- Failure Scenario 2 ---
    @Test
    
    void testHealth_whenGetConnectionThrowsException_shouldReturnDown() throws SQLException {
        SQLException testException = new SQLException("Test Connection Error");
        when(mockDataSource.getConnection()).thenThrow(testException);

        Health health = databaseHealthIndicator.health();

        // Assert
        assertNotNull(health);
        assertEquals(Status.DOWN, health.getStatus(), "Health status should be DOWN when an exception occurs");
        assertEquals("Failed to get connection", health.getDetails().get("database"));
        
        assertTrue(health.getDetails().containsKey("error"), "Details should contain an 'error' key");
        
        Object errorDetail = health.getDetails().get("error");
        assertInstanceOf(String.class, errorDetail, "The 'error' detail should be a String");
        
        String errorString = (String) errorDetail;
        assertTrue(errorString.contains("java.sql.SQLException"), "Error string should contain the exception type");
        assertTrue(errorString.contains("Test Connection Error"), "Error string should contain the exception message");
    }
}