package com.epam.gym_crm.controller.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.epam.gym_crm.controller.RestAuthenticationController;
import com.epam.gym_crm.dto.request.ChangePasswordRequest;
import com.epam.gym_crm.dto.request.LoginRequest;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.service.IAuthenticationService;

@ExtendWith(MockitoExtension.class)
class RestAuthenticationControllerTest {

    @Mock
    private IAuthenticationService authenticationService;

    @InjectMocks
    private RestAuthenticationController authController;

    // --- Login Testleri ---

    @Test
    void testLogin_Success() {
        String username = "test.user";
        String password = "password123";
        doNothing().when(authenticationService).login(any(LoginRequest.class));

        ResponseEntity<String> response = authController.login(username, password);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Beklenen mesajı, controller'ın döndürdüğü dinamik mesajla eşleştir
        String expectedMessage = "Login successful for user: " + username;
        assertEquals(expectedMessage, response.getBody());
        
        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_Failure_ServiceThrowsException() {
        String username = "wrong.user";
        String password = "wrongpassword";
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.UNAUTHORIZED, "Invalid credentials");
        
        doThrow(new BaseException(expectedErrorMessage))
            .when(authenticationService).login(any(LoginRequest.class));

        BaseException exception = assertThrows(BaseException.class, () -> {
            authController.login(username, password);
        });

        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
    }

    // --- Change Password Testleri ---

    @Test
    void testChangePassword_Success() {
        // Arrange
        String username = "test.user"; 
        ChangePasswordRequest request = new ChangePasswordRequest(username, "oldPass", "newPass");
        doNothing().when(authenticationService).changePassword(request);
        
        // Act
        ResponseEntity<String> response = authController.changePassword(request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        String expectedMessage = "Password changed successfully for user: " + username;
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

    // --- Logout Testleri ---
    @Test
    void testLogout_Success() {
        doNothing().when(authenticationService).logout();
        
        ResponseEntity<String> response = authController.logout();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Doğrudan String yanıtını kontrol et
        assertEquals("Logout successful", response.getBody());
        verify(authenticationService, times(1)).logout();
    }
}