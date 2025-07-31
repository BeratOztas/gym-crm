package com.epam.gym_crm.controller.impl;

import com.epam.gym_crm.controller.ApiResponse;
import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerCreateRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerTrainingListRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerUpdateRequest;
import com.epam.gym_crm.dto.response.TrainerProfileResponse;
import com.epam.gym_crm.dto.response.TrainerTrainingInfoResponse;
import com.epam.gym_crm.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.service.ITrainerService;
import com.epam.gym_crm.service.ITrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestTrainerControllerImplTest {

    @Mock
    private ITrainerService trainerService;
    @Mock
    private ITrainingService trainingService;

    @InjectMocks
    private RestTrainerControllerImpl trainerController;

    // --- createTrainer Testleri ---

    @Test
    void testCreateTrainer_Success() {
        // Arrange
        TrainerCreateRequest request = new TrainerCreateRequest("Peter", "Jones", "FITNESS");
        UserRegistrationResponse registrationResponse = new UserRegistrationResponse("Peter.Jones", "password");
        
        when(trainerService.createTrainer(request)).thenReturn(registrationResponse);

        // Act
        ResponseEntity<ApiResponse<UserRegistrationResponse>> response = trainerController.createTrainer(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(registrationResponse, response.getBody().getPayload());
        verify(trainerService, times(1)).createTrainer(request);
    }

    // --- findTrainerByUsername Testleri ---
    
    @Test
    void testFindTrainerByUsername_Success() {
        // Arrange
        String username = "Peter.Jones";
        TrainerProfileResponse profileResponse = new TrainerProfileResponse();
        when(trainerService.findTrainerByUsername(username)).thenReturn(profileResponse);

        // Act
        ResponseEntity<ApiResponse<TrainerProfileResponse>> response = trainerController.findTrainerByUsername(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profileResponse, response.getBody().getPayload());
        verify(trainerService, times(1)).findTrainerByUsername(username);
    }

    @Test
    void testFindTrainerByUsername_Failure_NotFound() {
        // Arrange
        String username = "non.existent";
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainer not found");
        when(trainerService.findTrainerByUsername(username)).thenThrow(new BaseException(expectedErrorMessage));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            trainerController.findTrainerByUsername(username);
        });
        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
    }

    // --- updateTrainer Testleri ---
    
    @Test
    void testUpdateTrainer_Success() {
        // Arrange
        String username = "Peter.Jones";
        TrainerUpdateRequest request = new TrainerUpdateRequest(username, "Pete", "Jones", "FITNESS", true);
        TrainerProfileResponse profileResponse = new TrainerProfileResponse();
        when(trainerService.updateTrainer(request)).thenReturn(profileResponse);

        // Act
        ResponseEntity<ApiResponse<TrainerProfileResponse>> response = trainerController.updateTrainer(username, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profileResponse, response.getBody().getPayload());
        verify(trainerService, times(1)).updateTrainer(request);
    }

    @Test
    void testUpdateTrainer_Failure_PathBodyMismatch() {
        // Arrange
        String urlUsername = "Peter.Jones";
        TrainerUpdateRequest request = new TrainerUpdateRequest("Different.User", "Pete", "Jones", "FITNESS", true);
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.PATH_BODY_MISMATCH, "URL username and request body username must match.");

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            trainerController.updateTrainer(urlUsername, request);
        });
        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
        // Servisin hiç çağrılmadığını doğrula
        verify(trainerService, never()).updateTrainer(any());
    }

    // --- activateDeactivateTrainer Testleri ---

    @Test
    void testActivateDeactivateTrainer_Success() {
        // Arrange
        String username = "Peter.Jones";
        UserActivationRequest request = new UserActivationRequest(username, false);
        doNothing().when(trainerService).activateDeactivateTrainer(request);

        // Act
        ResponseEntity<ApiResponse<?>> response = trainerController.activateDeactivateTrainer(username, request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Trainer status updated successfully.", response.getBody().getPayload());
        verify(trainerService, times(1)).activateDeactivateTrainer(request);
    }
    
    // --- deleteTrainerByUsername Testleri ---

    @Test
    void testDeleteTrainerByUsername_Success() {
        // Arrange
        String username = "Peter.Jones";
        doNothing().when(trainerService).deleteTrainerByUsername(username);

        // Act
        ResponseEntity<ApiResponse<?>> response = trainerController.deleteTrainerByUsername(username);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(trainerService, times(1)).deleteTrainerByUsername(username);
    }

    // --- getTrainerTrainingsList Testleri ---

    @Test
    void testGetTrainerTrainingsList_Success() {
        // Arrange
        String username = "Peter.Jones";
        TrainerTrainingListRequest request = new TrainerTrainingListRequest(); 
        List<TrainerTrainingInfoResponse> trainingList = Collections.singletonList(new TrainerTrainingInfoResponse());
        
        when(trainingService.getTrainerTrainingsList(username, request)).thenReturn(trainingList);

        // Act
        ResponseEntity<ApiResponse<List<TrainerTrainingInfoResponse>>> response = trainerController.getTrainerTrainingsList(username, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(trainingList, response.getBody().getPayload());
        verify(trainingService, times(1)).getTrainerTrainingsList(username, request);
    }
}