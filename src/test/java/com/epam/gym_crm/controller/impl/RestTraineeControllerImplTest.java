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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.epam.gym_crm.controller.ApiResponse;
import com.epam.gym_crm.dto.request.trainee.TraineeCreateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.dto.response.TraineeProfileResponse;
import com.epam.gym_crm.dto.response.TrainerInfoResponse;
import com.epam.gym_crm.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.service.ITraineeService;
import com.epam.gym_crm.service.ITrainerService;
import com.epam.gym_crm.service.ITrainingService;

@ExtendWith(MockitoExtension.class)
class RestTraineeControllerImplTest {

    @Mock
    private ITraineeService traineeService;
    @Mock
    private ITrainerService trainerService;
    @Mock
    private ITrainingService trainingService;

    @InjectMocks
    private RestTraineeControllerImpl traineeController;

    // --- createTrainee Testleri ---

    @Test
    void testCreateTrainee_Success() {
        // Arrange
        TraineeCreateRequest request = new TraineeCreateRequest("John", "Doe", LocalDate.now(), "123 Street");
        UserRegistrationResponse registrationResponse = new UserRegistrationResponse("John.Doe", "password");
        
        when(traineeService.createTrainee(request)).thenReturn(registrationResponse);

        // Act
        ResponseEntity<ApiResponse<UserRegistrationResponse>> response = traineeController.createTrainee(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(registrationResponse, response.getBody().getPayload());
        verify(traineeService, times(1)).createTrainee(request);
    }

    // --- findTraineeByUsername Testleri ---
    
    @Test
    void testFindTraineeByUsername_Success() {
        // Arrange
        String username = "John.Doe";
        TraineeProfileResponse profileResponse = new TraineeProfileResponse(); // İçini doldurmaya gerek yok
        when(traineeService.findTraineeByUsername(username)).thenReturn(profileResponse);

        // Act
        ResponseEntity<ApiResponse<TraineeProfileResponse>> response = traineeController.findTraineeByUsername(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profileResponse, response.getBody().getPayload());
        verify(traineeService, times(1)).findTraineeByUsername(username);
    }

    @Test
    void testFindTraineeByUsername_Failure_NotFound() {
        // Arrange
        String username = "non.existent";
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainee not found");
        when(traineeService.findTraineeByUsername(username)).thenThrow(new BaseException(expectedErrorMessage));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            traineeController.findTraineeByUsername(username);
        });
        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
    }

    // --- updateTrainee Testleri ---
    
    @Test
    void testUpdateTrainee_Success() {
        // Arrange
        String username = "John.Doe";
        TraineeUpdateRequest request = new TraineeUpdateRequest(username, "Johnny", "Doe", null, "456 Avenue", true);
        TraineeProfileResponse profileResponse = new TraineeProfileResponse();
        when(traineeService.updateTrainee(request)).thenReturn(profileResponse);

        // Act
        ResponseEntity<ApiResponse<TraineeProfileResponse>> response = traineeController.updateTrainee(username, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(profileResponse, response.getBody().getPayload());
        verify(traineeService, times(1)).updateTrainee(request);
    }

    @Test
    void testUpdateTrainee_Failure_PathBodyMismatch() {
        // Arrange
        String urlUsername = "John.Doe";
        TraineeUpdateRequest request = new TraineeUpdateRequest("Jane.Doe", "Jane", "Doe", null, "456 Avenue", true);
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.PATH_BODY_MISMATCH, "URL username and request body username must match.");

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            traineeController.updateTrainee(urlUsername, request);
        });
        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
        // Servisin hiç çağrılmadığını doğrula
        verify(traineeService, never()).updateTrainee(any());
    }

    // --- deleteTraineeByUsername Testleri ---

    @Test
    void testDeleteTraineeByUsername_Success() {
        // Arrange
        String username = "John.Doe";
        doNothing().when(traineeService).deleteTraineeByUsername(username);

        // Act
        ResponseEntity<ApiResponse<?>> response = traineeController.deleteTraineeByUsername(username);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(traineeService, times(1)).deleteTraineeByUsername(username);
    }

    // --- Diğer Önemli Metotlar için Örnekler ---
    
    @Test
    void testGetUnassignedTrainersForTrainee_Success() {
        // Arrange
        String username = "John.Doe";
        List<TrainerInfoResponse> trainerList = Collections.singletonList(new TrainerInfoResponse());
        when(trainerService.getUnassignedTrainersForTrainee(username)).thenReturn(trainerList);

        // Act
        ResponseEntity<ApiResponse<List<TrainerInfoResponse>>> response = traineeController.getUnassignedTrainersForTrainee(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(trainerList, response.getBody().getPayload());
        verify(trainerService, times(1)).getUnassignedTrainersForTrainee(username);
    }
    
    @Test
    void testUpdateTraineeTrainersList_Success() {
        // Arrange
        String username = "John.Doe";
        TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest(username, List.of("Trainer.One"));
        List<TrainerInfoResponse> trainerList = Collections.singletonList(new TrainerInfoResponse());
        when(traineeService.updateTraineeTrainersList(request)).thenReturn(trainerList);
        
        // Act
        ResponseEntity<ApiResponse<List<TrainerInfoResponse>>> response = traineeController.updateTraineeTrainersList(username, request);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(trainerList, response.getBody().getPayload());
        verify(traineeService, times(1)).updateTraineeTrainersList(request);
    }
}