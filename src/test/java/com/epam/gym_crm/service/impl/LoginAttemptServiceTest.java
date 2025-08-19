package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym_crm.config.LoginSecurityConfig;
import com.epam.gym_crm.domain.service.impl.LoginAttemptService;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private LoginSecurityConfig loginSecurityConfig;

    private LoginAttemptService loginAttemptService;

    private final String TEST_USERNAME = "test.user";

    @BeforeEach
    void setUp() {
        when(loginSecurityConfig.getMaxAttempts()).thenReturn(3);
        
        when(loginSecurityConfig.getLockoutDurationMinutes()).thenReturn(5L);

        loginAttemptService = new LoginAttemptService(loginSecurityConfig);
    }

    // -----  (Success Scenarios) -----

    @Test
    void shouldNotBeBlockedWhenAttemptsAreBelowMax() {
        // Arrange
        loginAttemptService.loginFailed(TEST_USERNAME); // 1
        loginAttemptService.loginFailed(TEST_USERNAME); // 2

        // Act
        boolean isBlocked = loginAttemptService.isBlocked(TEST_USERNAME);

        // Assert
        assertFalse(isBlocked, "User should not be blocked after 2 failed attempts.");
    }

    @Test
    void shouldResetAttemptsOnSuccessfulLogin() {
        // Arrange
        loginAttemptService.loginFailed(TEST_USERNAME); // 1
        loginAttemptService.loginFailed(TEST_USERNAME); // 2

        // Act
        loginAttemptService.loginSucceeded(TEST_USERNAME);
        boolean isBlocked = loginAttemptService.isBlocked(TEST_USERNAME);

        // Assert
        assertFalse(isBlocked, "Counter should be reset, so user should not be blocked.");
    }

    @Test
    void shouldAllowLoginForUserWithNoPreviousAttempts() {

        // Act
        boolean isBlocked = loginAttemptService.isBlocked(TEST_USERNAME);
        
        // Assert
        assertFalse(isBlocked, "A user with no history should not be blocked.");
    }


    // ---  (Failure & Blocking Scenarios) ---

    @Test
    void shouldBeBlockedWhenAttemptsReachMax() {
        // Arrange
        loginAttemptService.loginFailed(TEST_USERNAME); // 1
        loginAttemptService.loginFailed(TEST_USERNAME); // 2
        loginAttemptService.loginFailed(TEST_USERNAME); // 3

        // Act
        boolean isBlocked = loginAttemptService.isBlocked(TEST_USERNAME);

        // Assert
        assertTrue(isBlocked, "User should be blocked after 3 failed attempts.");
    }

    @Test
    void shouldRemainBlockedWhenAttemptsExceedMax() {
        // Arrange
        loginAttemptService.loginFailed(TEST_USERNAME); // 1
        loginAttemptService.loginFailed(TEST_USERNAME); // 2
        loginAttemptService.loginFailed(TEST_USERNAME); // 3 (Kilitlendi)
        loginAttemptService.loginFailed(TEST_USERNAME); // 4

        // Act
        boolean isBlocked = loginAttemptService.isBlocked(TEST_USERNAME);

        // Assert
        assertTrue(isBlocked, "User should remain blocked after more than 3 failed attempts.");
    }
    
    @Test
    void shouldResetAttemptsAndUnblockAfterSuccessfulLogin() {
    	
        loginAttemptService.loginFailed(TEST_USERNAME); // 1
        loginAttemptService.loginFailed(TEST_USERNAME); // 2
        loginAttemptService.loginFailed(TEST_USERNAME); // 3 (Kilitlendi)
        
        assertTrue(loginAttemptService.isBlocked(TEST_USERNAME), "Pre-condition failed: User should be blocked.");

        loginAttemptService.loginSucceeded(TEST_USERNAME); 
        
        assertFalse(loginAttemptService.isBlocked(TEST_USERNAME), "User should be unblocked after a successful login.");
    }
    
    @Test
    void shouldHandleDifferentUsersIndependently() {
        // Arrange
        String user1 = "user.one";
        String user2 = "user.two";

        loginAttemptService.loginFailed(user1); 
        loginAttemptService.loginFailed(user1); 
        
        loginAttemptService.loginFailed(user2); 
        // Act
        boolean isUser1Blocked = loginAttemptService.isBlocked(user1);
        boolean isUser2Blocked = loginAttemptService.isBlocked(user2);

        assertFalse(isUser1Blocked, "User1 should not be blocked yet.");
        assertFalse(isUser2Blocked, "User2's attempts should not affect User1.");
        
        loginAttemptService.loginFailed(user1); 
        
        isUser1Blocked = loginAttemptService.isBlocked(user1);
        isUser2Blocked = loginAttemptService.isBlocked(user2);

        assertTrue(isUser1Blocked, "User1 should now be blocked.");
        assertFalse(isUser2Blocked, "Blocking User1 should not block User2.");
    }
}
