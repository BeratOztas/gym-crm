package com.epam.gym_crm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.epam.gym_crm.api.controller.RestAuthenticationController;
import com.epam.gym_crm.api.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.api.dto.request.LoginRequest;
import com.epam.gym_crm.api.dto.response.LoginResponse;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.ErrorMessage;
import com.epam.gym_crm.domain.exception.MessageType;
import com.epam.gym_crm.domain.service.IAuthenticationService;
import com.epam.gym_crm.security.JwtTokenExtractor;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationControllerTest {

    @Mock
    private IAuthenticationService authenticationService;

    @Mock
    private JwtTokenExtractor jwtTokenExtractor;

    @InjectMocks
    private RestAuthenticationController authController;

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
    }
    // --- Login Tests ---

    @Test
    void testLogin_Success() {
        LoginRequest loginRequest = new LoginRequest("test.user", "password123");
        LoginResponse loginResponse = new LoginResponse("test.user", "mock_jwt_token");

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());
        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_Failure_ServiceThrowsException() {
        LoginRequest loginRequest = new LoginRequest("wrong.user", "wrongpassword");
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid credentials");

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new BaseException(expectedErrorMessage));

        BaseException exception = assertThrows(BaseException.class, () -> {
            authController.login(loginRequest);
        });

        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
    }

    // --- Change Password Tests ---

    @Test
    void testChangePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("test.user", "oldPass", "newPass");
        doNothing().when(authenticationService).changePassword(request);

        ResponseEntity<String> response = authController.changePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String expectedMessage = "Password changed successfully for user: " + request.getUsername();
        assertEquals(expectedMessage, response.getBody());
        verify(authenticationService, times(1)).changePassword(request);
    }

    @Test
    void testChangePassword_Failure_InvalidOldPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("test.user", "wrongOldPass", "newPass");
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid old password.");

        doThrow(new BaseException(expectedErrorMessage))
                .when(authenticationService).changePassword(request);

        BaseException exception = assertThrows(BaseException.class, () -> {
            authController.changePassword(request);
        });

        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
    }

    // --- Logout Tests ---
    @Test
    void testLogout_Success() {
        String mockToken = "mock_jwt_token";
        when(jwtTokenExtractor.extractJwtFromRequest(any(HttpServletRequest.class))).thenReturn(mockToken);
        doNothing().when(authenticationService).logout(eq(mockToken));

        ResponseEntity<String> response = authController.logout(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful", response.getBody());
        verify(authenticationService, times(1)).logout(eq(mockToken));
    }
}
