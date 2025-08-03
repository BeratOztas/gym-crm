package com.epam.gym_crm.controller.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.epam.gym_crm.controller.RestTrainerController;
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

@ExtendWith(MockitoExtension.class)
class RestTrainerControllerTest {

    @Mock
    private ITrainerService trainerService;
    @Mock
    private ITrainingService trainingService;

    @InjectMocks
    private RestTrainerController trainerController;

    // --- createTrainer Testleri ---

    @Test
    void testCreateTrainer_Success() {
        // Arrange
        TrainerCreateRequest request = new TrainerCreateRequest("Peter", "Jones", "FITNESS");
        UserRegistrationResponse registrationResponse = new UserRegistrationResponse("Peter.Jones", "password");
        
        when(trainerService.createTrainer(request)).thenReturn(registrationResponse);

        // Act
        ResponseEntity<UserRegistrationResponse> response = trainerController.createTrainer(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        // Doğrudan DTO'yu kontrol et
        assertEquals(registrationResponse, response.getBody());
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
        ResponseEntity<TrainerProfileResponse> response = trainerController.findTrainerByUsername(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Doğrudan DTO'yu kontrol et
        assertEquals(profileResponse, response.getBody());
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
        ResponseEntity<TrainerProfileResponse> response = trainerController.updateTrainer(username, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Doğrudan DTO'yu kontrol et
        assertEquals(profileResponse, response.getBody());
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
        ResponseEntity<?> response = trainerController.activateDeactivateTrainer(username, request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Doğrudan String yanıtını kontrol et
        assertEquals("Trainer status updated successfully.", response.getBody());
        verify(trainerService, times(1)).activateDeactivateTrainer(request);
    }
    
    // --- deleteTrainerByUsername Testleri ---

    @Test
    void testDeleteTrainerByUsername_Success() {
        // Arrange
        String username = "Peter.Jones";
        doNothing().when(trainerService).deleteTrainerByUsername(username);

        // Act
        ResponseEntity<?> response = trainerController.deleteTrainerByUsername(username);

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
        ResponseEntity<List<TrainerTrainingInfoResponse>> response = trainerController.getTrainerTrainingsList(username, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Doğrudan DTO listesini kontrol et
        assertEquals(trainingList, response.getBody());
        verify(trainingService, times(1)).getTrainerTrainingsList(username, request);
    }
}