package com.epam.gym_crm.controller.impl;

import com.epam.gym_crm.controller.ApiResponse;
import com.epam.gym_crm.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.dto.request.LoginRequest;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.service.IAuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationControllerImplTest {

    @Mock
    private IAuthenticationService authenticationService;

    @InjectMocks
    private RestAuthenticationControllerImpl authController;

    // --- Login Testleri ---

    @Test
    void testLogin_Success() {
        String username = "test.user";
        String password = "password123";
        doNothing().when(authenticationService).login(any(LoginRequest.class));
        ResponseEntity<ApiResponse<String>> response = authController.login(username, password);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful.", response.getBody().getPayload());
        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_Failure_ServiceThrowsException() {
        // Arrange
        String username = "wrong.user";
        String password = "wrongpassword";
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid credentials");
        
        doThrow(new BaseException(expectedErrorMessage))
            .when(authenticationService).login(any(LoginRequest.class));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            authController.login(username, password);
        });

        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
    }

    // --- Change Password Testleri ---

    @Test
    void testChangePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("test.user", "oldPass", "newPass");
        doNothing().when(authenticationService).changePassword(request);
        ResponseEntity<ApiResponse<String>> response = authController.changePassword(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password changed successfully", response.getBody().getPayload());
        verify(authenticationService, times(1)).changePassword(request);
    }

    @Test
    void testChangePassword_Failure_InvalidOldPassword() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest("test.user", "wrongOldPass", "newPass");
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid old password.");

        doThrow(new BaseException(expectedErrorMessage))
            .when(authenticationService).changePassword(request);
            
        BaseException exception = assertThrows(BaseException.class, () -> {
            authController.changePassword(request);
        });

        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
    }

    // --- Logout Testleri ---
    @Test
    void testLogout_Success() {
        doNothing().when(authenticationService).logout();
        ResponseEntity<ApiResponse<String>> response = authController.logout();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful", response.getBody().getPayload());
        verify(authenticationService, times(1)).logout();
    }
}