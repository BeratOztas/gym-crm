package com.epam.gym_crm.service.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.service.impl.LoginAttemptService;
import com.epam.gym_crm.domain.service.impl.UserDetailsServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;
    private final String TEST_USERNAME = "test.user";
    private final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword("encodedPassword");
        testUser.setActive(true);
    }

    // --- loadUserByUsername Tests ---

    @Test
    void shouldLoadUserByUsernameSuccessfullyWhenAccountIsNotLocked() {
    	
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(loginAttemptService.isBlocked(TEST_USERNAME)).thenReturn(false);

        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_USERNAME);

        assertNotNull(userDetails);
        assertEquals(TEST_USERNAME, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled(), "User should be enabled (active).");
        assertTrue(userDetails.isAccountNonLocked(), "Account should not be locked.");
    }

    @Test
    void shouldLoadUserByUsernameSuccessfullyWhenAccountIsLocked() {
    	
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(loginAttemptService.isBlocked(TEST_USERNAME)).thenReturn(true);

        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_USERNAME);

        assertNotNull(userDetails);
        assertEquals(TEST_USERNAME, userDetails.getUsername());
        assertFalse(userDetails.isAccountNonLocked(), "Account should be locked.");
    }

    @Test
    void shouldThrowBaseExceptionWhenUserNotFoundByUsername() {
    	
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> {
            userDetailsService.loadUserByUsername(TEST_USERNAME);
        });
    }

    // --- loadUserById Tests ---

    @Test
    void shouldLoadUserByIdSuccessfully() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        
        UserDetails userDetails = userDetailsService.loadUserById(TEST_USER_ID);

        assertNotNull(userDetails);
        assertEquals(TEST_USERNAME, userDetails.getUsername());
        
        assertTrue(userDetails.isAccountNonLocked(), "loadUserById should always assume account is not locked.");
    }
    
    @Test
    void shouldThrowBaseExceptionWhenUserNotFoundById() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> {
            userDetailsService.loadUserById(TEST_USER_ID);
        });
    }
}
