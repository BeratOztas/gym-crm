package com.epam.gym_crm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.epam.gym_crm.api.controller.RestTraineeController;
import com.epam.gym_crm.api.dto.request.trainee.TraineeCreateRequest;
import com.epam.gym_crm.api.dto.request.trainee.TraineeUpdateRequest;
import com.epam.gym_crm.api.dto.request.trainee.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.api.dto.response.TraineeProfileResponse;
import com.epam.gym_crm.api.dto.response.TrainerInfoResponse;
import com.epam.gym_crm.api.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.ErrorMessage;
import com.epam.gym_crm.domain.exception.MessageType;
import com.epam.gym_crm.domain.service.ITraineeService;
import com.epam.gym_crm.domain.service.ITrainerService;
import com.epam.gym_crm.domain.service.ITrainingService;

@ExtendWith(MockitoExtension.class)
class RestTraineeControllerTest {

	@Mock
	private ITraineeService traineeService;
	@Mock
	private ITrainerService trainerService;
	@Mock
	private ITrainingService trainingService;

	@InjectMocks
	private RestTraineeController traineeController;

	// --- createTrainee Tests---

	@Test
	void testCreateTrainee_Success() {
		TraineeCreateRequest request = new TraineeCreateRequest("John", "Doe", LocalDate.now(), "123 Street");
		
		UserRegistrationResponse registrationResponse = new UserRegistrationResponse("John.Doe", "password",
				"mock_access_token");

		when(traineeService.createTrainee(request)).thenReturn(registrationResponse);

		ResponseEntity<UserRegistrationResponse> response = traineeController.createTrainee(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(registrationResponse, response.getBody());
		verify(traineeService, times(1)).createTrainee(request);
	}

	// --- findTraineeByUsername Tests ---

	@Test
	void testFindTraineeByUsername_Success() {
		
		String username = "John.Doe";
		TraineeProfileResponse profileResponse = new TraineeProfileResponse();
		when(traineeService.findTraineeByUsername(username)).thenReturn(profileResponse);

		ResponseEntity<TraineeProfileResponse> response = traineeController.findTraineeByUsername(username);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		assertEquals(profileResponse, response.getBody());
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

	// --- updateTrainee Tests ---

	@Test
	void testUpdateTrainee_Success() {
		// Arrange
		String username = "John.Doe";
		TraineeUpdateRequest request = new TraineeUpdateRequest(username, "Johnny", "Doe", null, "456 Avenue", true);
		TraineeProfileResponse profileResponse = new TraineeProfileResponse();
		when(traineeService.updateTrainee(request)).thenReturn(profileResponse);

		ResponseEntity<TraineeProfileResponse> response = traineeController.updateTrainee(username, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(profileResponse, response.getBody());
		verify(traineeService, times(1)).updateTrainee(request);
	}

	@Test
	void testUpdateTrainee_Failure_PathBodyMismatch() {
		// Arrange
		String urlUsername = "John.Doe";
		TraineeUpdateRequest request = new TraineeUpdateRequest("Jane.Doe", "Jane", "Doe", null, "456 Avenue", true);
		ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.PATH_BODY_MISMATCH,
				"URL username and request body username must match.");

		// Act & Assert
		BaseException exception = assertThrows(BaseException.class, () -> {
			traineeController.updateTrainee(urlUsername, request);
		});
		assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
		verify(traineeService, never()).updateTrainee(any());
	}

	// --- deleteTraineeByUsername Tests ---

	@Test
	void testDeleteTraineeByUsername_Success() {
		String username = "John.Doe";
		doNothing().when(traineeService).deleteTraineeByUsername(username);

		ResponseEntity<?> response = traineeController.deleteTraineeByUsername(username);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

		assertEquals(null, response.getBody());
		verify(traineeService, times(1)).deleteTraineeByUsername(username);
	}


	@Test
	void testGetUnassignedTrainersForTrainee_Success() {
		
		String username = "John.Doe";
		List<TrainerInfoResponse> trainerList = Collections.singletonList(new TrainerInfoResponse());
		when(trainerService.getUnassignedTrainersForTrainee(username)).thenReturn(trainerList);

		ResponseEntity<List<TrainerInfoResponse>> response = traineeController
				.getUnassignedTrainersForTrainee(username);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		assertEquals(trainerList, response.getBody());
		verify(trainerService, times(1)).getUnassignedTrainersForTrainee(username);
	}

	@Test
	void testUpdateTraineeTrainersList_Success() {
		
		String username = "John.Doe";
		TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest(username, List.of("Trainer.One"));
		List<TrainerInfoResponse> trainerList = Collections.singletonList(new TrainerInfoResponse());
		when(traineeService.updateTraineeTrainersList(request)).thenReturn(trainerList);

		ResponseEntity<List<TrainerInfoResponse>> response = traineeController.updateTraineeTrainersList(username,
				request);

		
		assertEquals(HttpStatus.OK, response.getStatusCode());
		
		assertEquals(trainerList, response.getBody());
		verify(traineeService, times(1)).updateTraineeTrainersList(request);
	}
}