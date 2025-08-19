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

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.epam.gym_crm.api.controller.RestTrainingController;
import com.epam.gym_crm.api.dto.request.training.TrainingCreateRequest;
import com.epam.gym_crm.api.dto.request.training.TrainingUpdateRequest;
import com.epam.gym_crm.api.dto.response.TrainingResponse;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.ErrorMessage;
import com.epam.gym_crm.domain.exception.MessageType;
import com.epam.gym_crm.domain.service.ITrainingService;

@ExtendWith(MockitoExtension.class)
class RestTrainingControllerTest {

	@Mock
	private ITrainingService trainingService;

	@InjectMocks
	private RestTrainingController trainingController;

	// --- createTraining Tests ---

	@Test
	void testCreateTraining_Success() {
		
	    TrainingCreateRequest request = new TrainingCreateRequest("Trainee.User", "Trainer.User", "Cardio", LocalDate.now(), 60);
	    TrainingResponse trainingResponse = new TrainingResponse(); 
	    
	    when(trainingService.createTraining(request)).thenReturn(trainingResponse);

	    
	    ResponseEntity<?> response = trainingController.createTraining(request);

	    
	    assertEquals(HttpStatus.CREATED, response.getStatusCode());
	    
	    assertEquals("Training created successfully.", response.getBody());
	    verify(trainingService, times(1)).createTraining(request);
	}

	// --- getTrainingById Tests ---

	@Test
	void testGetTrainingById_Success() {
		
		Long trainingId = 1L;
		TrainingResponse trainingResponse = new TrainingResponse();
		when(trainingService.getTrainingById(trainingId)).thenReturn(trainingResponse);

		
		ResponseEntity<TrainingResponse> response = trainingController.getTrainingById(trainingId);

		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		assertEquals(trainingResponse, response.getBody());
		verify(trainingService, times(1)).getTrainingById(trainingId);
	}

	@Test
	void testGetTrainingById_Failure_NotFound() {
		Long trainingId = 99L;
		ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Training not found");
		when(trainingService.getTrainingById(trainingId)).thenThrow(new BaseException(expectedErrorMessage));

		BaseException exception = assertThrows(BaseException.class, () -> {
			trainingController.getTrainingById(trainingId);
		});
		assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
	}

	// --- updateTraining Tests---

	@Test
	void testUpdateTraining_Success() {
		Long trainingId = 1L;
		TrainingUpdateRequest request = new TrainingUpdateRequest();
		request.setId(trainingId);

		TrainingResponse trainingResponse = new TrainingResponse();
		when(trainingService.updateTraining(request)).thenReturn(trainingResponse);

		ResponseEntity<TrainingResponse> response = trainingController.updateTraining(trainingId, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		assertEquals(trainingResponse, response.getBody());
		verify(trainingService, times(1)).updateTraining(request);
	}

	@Test
	void testUpdateTraining_Failure_PathBodyMismatch() {
		Long urlId = 1L;
		TrainingUpdateRequest request = new TrainingUpdateRequest();
		request.setId(2L);

		ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.PATH_BODY_MISMATCH,
				"URL ID and request body ID must match.");

		BaseException exception = assertThrows(BaseException.class, () -> {
			trainingController.updateTraining(urlId, request);
		});
		assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
		
		verify(trainingService, never()).updateTraining(any());
	}

	// --- deleteTrainingById Tests ---

	@Test
	void testDeleteTrainingById_Success() {
		
		Long trainingId = 1L;
		doNothing().when(trainingService).deleteTrainingById(trainingId);

		ResponseEntity<?> response = trainingController.deleteTrainingById(trainingId);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertNull(response.getBody());
		verify(trainingService, times(1)).deleteTrainingById(trainingId);
	}
}