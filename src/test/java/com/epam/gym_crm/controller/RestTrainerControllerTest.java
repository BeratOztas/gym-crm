package com.epam.gym_crm.controller;

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

import com.epam.gym_crm.api.controller.RestTrainerController;
import com.epam.gym_crm.api.dto.request.UserActivationRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerCreateRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerTrainingListRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerUpdateRequest;
import com.epam.gym_crm.api.dto.response.TrainerProfileResponse;
import com.epam.gym_crm.api.dto.response.TrainerTrainingInfoResponse;
import com.epam.gym_crm.api.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.ErrorMessage;
import com.epam.gym_crm.domain.exception.MessageType;
import com.epam.gym_crm.domain.service.ITrainerService;
import com.epam.gym_crm.domain.service.ITrainingService;

@ExtendWith(MockitoExtension.class)
class RestTrainerControllerTest {

    @Mock
    private ITrainerService trainerService;
    @Mock
    private ITrainingService trainingService;

    @InjectMocks
    private RestTrainerController trainerController;

    // --- createTrainer Tests ---

    @Test
    void testCreateTrainer_Success() {
        // Arrange
        TrainerCreateRequest request = new TrainerCreateRequest("Peter", "Jones", "FITNESS");
        
        UserRegistrationResponse registrationResponse = new UserRegistrationResponse("Peter.Jones", "password", "mock_access_token");
        
        when(trainerService.createTrainer(request)).thenReturn(registrationResponse);

        ResponseEntity<UserRegistrationResponse> response = trainerController.createTrainer(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(registrationResponse, response.getBody());
        verify(trainerService, times(1)).createTrainer(request);
    } 

    // --- findTrainerByUsername Tests ---
    
    @Test
    void testFindTrainerByUsername_Success() {
    	
        String username = "Peter.Jones";
        TrainerProfileResponse profileResponse = new TrainerProfileResponse();
        when(trainerService.findTrainerByUsername(username)).thenReturn(profileResponse);

        ResponseEntity<TrainerProfileResponse> response = trainerController.findTrainerByUsername(username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        assertEquals(profileResponse, response.getBody());
        verify(trainerService, times(1)).findTrainerByUsername(username);
    }

    @Test
    void testFindTrainerByUsername_Failure_NotFound() {
    	
        String username = "non.existent";
        
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainer not found");
        when(trainerService.findTrainerByUsername(username)).thenThrow(new BaseException(expectedErrorMessage));

        BaseException exception = assertThrows(BaseException.class, () -> {
            trainerController.findTrainerByUsername(username);
        });
        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
    }

    // --- updateTrainer Tests ---
    
    @Test
    void testUpdateTrainer_Success() {
    	
        String username = "Peter.Jones";
        TrainerUpdateRequest request = new TrainerUpdateRequest(username, "Pete", "Jones", "FITNESS", true);
        TrainerProfileResponse profileResponse = new TrainerProfileResponse();
        when(trainerService.updateTrainer(request)).thenReturn(profileResponse);

        ResponseEntity<TrainerProfileResponse> response = trainerController.updateTrainer(username, request);

        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        assertEquals(profileResponse, response.getBody());
        verify(trainerService, times(1)).updateTrainer(request);
    }

    @Test
    void testUpdateTrainer_Failure_PathBodyMismatch() {
        String urlUsername = "Peter.Jones";
        TrainerUpdateRequest request = new TrainerUpdateRequest("Different.User", "Pete", "Jones", "FITNESS", true);
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.PATH_BODY_MISMATCH, "URL username and request body username must match.");

        BaseException exception = assertThrows(BaseException.class, () -> {
            trainerController.updateTrainer(urlUsername, request);
        });
        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
        
        verify(trainerService, never()).updateTrainer(any());
    }

    // --- activateDeactivateTrainer Tests ---

    @Test
    void testActivateDeactivateTrainer_Success() {
    	
        String username = "Peter.Jones";
        UserActivationRequest request = new UserActivationRequest(username, false);
        doNothing().when(trainerService).activateDeactivateTrainer(request);

        ResponseEntity<?> response = trainerController.activateDeactivateTrainer(username, request);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        assertEquals("Trainer status updated successfully.", response.getBody());
        verify(trainerService, times(1)).activateDeactivateTrainer(request);
    }
    
    // --- deleteTrainerByUsername Tests---

    @Test
    void testDeleteTrainerByUsername_Success() {
    	
        String username = "Peter.Jones";
        doNothing().when(trainerService).deleteTrainerByUsername(username);

        ResponseEntity<?> response = trainerController.deleteTrainerByUsername(username);

        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(trainerService, times(1)).deleteTrainerByUsername(username);
    }

    // --- getTrainerTrainingsList Tests ---

    @Test
    void testGetTrainerTrainingsList_Success() {
    	
        String username = "Peter.Jones";
        TrainerTrainingListRequest request = new TrainerTrainingListRequest(); 
        List<TrainerTrainingInfoResponse> trainingList = Collections.singletonList(new TrainerTrainingInfoResponse());
        
        when(trainingService.getTrainerTrainingsList(username, request)).thenReturn(trainingList);

        
        ResponseEntity<List<TrainerTrainingInfoResponse>> response = trainerController.getTrainerTrainingsList(username, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        assertEquals(trainingList, response.getBody());
        verify(trainingService, times(1)).getTrainerTrainingsList(username, request);
    }
}