package com.epam.gym_crm.controller.impl;

import com.epam.gym_crm.controller.ApiResponse;
import com.epam.gym_crm.dto.request.training.TrainingCreateRequest;
import com.epam.gym_crm.dto.request.training.TrainingUpdateRequest;
import com.epam.gym_crm.dto.response.TrainingResponse;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.service.ITrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestTrainingControllerImplTest {

    @Mock
    private ITrainingService trainingService;

    @InjectMocks
    private RestTrainingControllerImpl trainingController;

    // --- createTraining Testleri ---

    @Test
    void testCreateTraining_Success() {
        // Arrange
        TrainingCreateRequest request = new TrainingCreateRequest("Trainee.User", "Trainer.User", "Cardio", LocalDate.now(), 60);
        TrainingResponse trainingResponse = new TrainingResponse(); 
        
        when(trainingService.createTraining(request)).thenReturn(trainingResponse);

        // Act
        ResponseEntity<ApiResponse<?>> response = trainingController.createTraining(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Training created successfully.", response.getBody().getPayload());
        verify(trainingService, times(1)).createTraining(request);
    }

    // --- getTrainingById Testleri ---
    
    @Test
    void testGetTrainingById_Success() {
        // Arrange
        Long trainingId = 1L;
        TrainingResponse trainingResponse = new TrainingResponse();
        when(trainingService.getTrainingById(trainingId)).thenReturn(trainingResponse);

        // Act
        ResponseEntity<ApiResponse<TrainingResponse>> response = trainingController.getTrainingById(trainingId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(trainingResponse, response.getBody().getPayload());
        verify(trainingService, times(1)).getTrainingById(trainingId);
    }

    @Test
    void testGetTrainingById_Failure_NotFound() {
        // Arrange
        Long trainingId = 99L;
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Training not found");
        when(trainingService.getTrainingById(trainingId)).thenThrow(new BaseException(expectedErrorMessage));

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            trainingController.getTrainingById(trainingId);
        });
        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
    }

    // --- updateTraining Testleri ---
    
    @Test
    void testUpdateTraining_Success() {
        // Arrange
        Long trainingId = 1L;
        TrainingUpdateRequest request = new TrainingUpdateRequest();
        request.setId(trainingId); // ID'leri eşleştir
        
        TrainingResponse trainingResponse = new TrainingResponse();
        when(trainingService.updateTraining(request)).thenReturn(trainingResponse);

        // Act
        ResponseEntity<ApiResponse<TrainingResponse>> response = trainingController.updateTraining(trainingId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(trainingResponse, response.getBody().getPayload());
        verify(trainingService, times(1)).updateTraining(request);
    }

    @Test
    void testUpdateTraining_Failure_PathBodyMismatch() {
        // Arrange
        Long urlId = 1L;
        TrainingUpdateRequest request = new TrainingUpdateRequest();
        request.setId(2L); // ID'ler uyuşmuyor
        
        ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.PATH_BODY_MISMATCH, "URL ID and request body ID must match.");

        // Act & Assert
        BaseException exception = assertThrows(BaseException.class, () -> {
            trainingController.updateTraining(urlId, request);
        });
        assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
        // Servisin hiç çağrılmadığını doğrula
        verify(trainingService, never()).updateTraining(any());
    }

    // --- deleteTrainingById Testleri ---

    @Test
    void testDeleteTrainingById_Success() {
        // Arrange
        Long trainingId = 1L;
        doNothing().when(trainingService).deleteTrainingById(trainingId);

        // Act
        ResponseEntity<ApiResponse<?>> response = trainingController.deleteTrainingById(trainingId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(trainingService, times(1)).deleteTrainingById(trainingId);
    }
}