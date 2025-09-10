package com.epam.gym_crm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
import com.epam.gym_crm.db.entity.Training;
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

	private final String MOCK_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

	// --- createTraining Tests ---

	@Test
	void testCreateTraining_Success() {
	    // Given
	    TrainingCreateRequest request = new TrainingCreateRequest("Trainee.User", "Trainer.User", "Cardio", LocalDate.now(), 60);
	    // trainingService'in void döndüğünü varsayıyoruz çünkü controller'da bir String dönüyor.
	    // The trainingService.createTraining method returns TrainingResponse, so we need to mock a return value.
	    when(trainingService.createTraining(any(TrainingCreateRequest.class), anyString()))
	        .thenReturn(new TrainingResponse(
	            new Training(1L, "Cardio", LocalDate.now(), 60, null, null, null)
	        ));

	    // When
	    ResponseEntity<?> response = trainingController.createTraining(request, MOCK_TOKEN);

	    // Then
	    assertEquals(HttpStatus.CREATED, response.getStatusCode());
	    assertEquals("Training created successfully.", response.getBody());
	    // Verify metodu token parametresiyle çağrıldı mı kontrol ediyoruz.
	    verify(trainingService, times(1)).createTraining(request, MOCK_TOKEN);
	}

	// --- getTrainingById Tests ---
	// Bu metodda token parametresi olmadığı için aynen kalabilir, sadece eksik olduğu için buraya taşıdım
	@Test
	void testGetTrainingById_Success() {
		// Given
		Long trainingId = 1L;
		TrainingResponse trainingResponse = new TrainingResponse();
		when(trainingService.getTrainingById(trainingId)).thenReturn(trainingResponse);

		// When
		ResponseEntity<TrainingResponse> response = trainingController.getTrainingById(trainingId);

		// Then
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(trainingResponse, response.getBody());
		verify(trainingService, times(1)).getTrainingById(trainingId);
	}

	@Test
	void testGetTrainingById_Failure_NotFound() {
		// Given
		Long trainingId = 99L;
		ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Training not found");
		when(trainingService.getTrainingById(trainingId)).thenThrow(new BaseException(expectedErrorMessage));

		// When
		BaseException exception = assertThrows(BaseException.class, () -> {
			trainingController.getTrainingById(trainingId);
		});

		// Then
		assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
		verify(trainingService, times(1)).getTrainingById(trainingId); // Doğru ID ile çağrıldığından emin ol
	}

	// --- updateTraining Tests---

	@Test
	void testUpdateTraining_Success() {
		// Given
		Long trainingId = 1L;
		TrainingUpdateRequest request = new TrainingUpdateRequest();
		request.setId(trainingId); // URL'deki ID ile body'deki ID'nin eşleşmesi gerekiyor
		// Diğer alanları da doldurabilirsiniz:
		// request.setTrainingName("Updated Cardio");
		// request.setTrainingDate(LocalDate.now());
		// request.setTrainingDuration(90);
		// request.setTrainerUsername("Trainer.User");

		TrainingResponse trainingResponse = new TrainingResponse();
		// trainingService'in updateTraining metodunun yeni parametreleri kabul ettiğini varsayıyoruz.
		when(trainingService.updateTraining(any(TrainingUpdateRequest.class), anyString())).thenReturn(trainingResponse);

		// When
		ResponseEntity<TrainingResponse> response = trainingController.updateTraining(trainingId, request, MOCK_TOKEN);

		// Then
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(trainingResponse, response.getBody());
		// Verify metodu token parametresiyle çağrıldı mı kontrol ediyoruz.
		verify(trainingService, times(1)).updateTraining(request, MOCK_TOKEN);
	}

	@Test
	void testUpdateTraining_Failure_PathBodyMismatch() {
		// Given
		Long urlId = 1L;
		TrainingUpdateRequest request = new TrainingUpdateRequest();
		request.setId(2L); // URL'deki ID ile body'deki ID farklı

		ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.PATH_BODY_MISMATCH,
				"URL ID and request body ID must match.");

		// When
		BaseException exception = assertThrows(BaseException.class, () -> {
			trainingController.updateTraining(urlId, request, MOCK_TOKEN); // Token parametresini ekledik
		});

		// Then
		assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
		// Path-Body mismatch olduğu için servis metodu çağrılmamalı
		verify(trainingService, never()).updateTraining(any(TrainingUpdateRequest.class), anyString());
	}

	@Test
	void testUpdateTraining_Failure_NotFound() {
		// Given
		Long trainingId = 99L;
		TrainingUpdateRequest request = new TrainingUpdateRequest();
		request.setId(trainingId);

		ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Training not found for ID: " + trainingId);
		// Servis, training bulunamazsa BaseException fırlatmalı
		when(trainingService.updateTraining(any(TrainingUpdateRequest.class), anyString()))
				.thenThrow(new BaseException(expectedErrorMessage));

		// When
		BaseException exception = assertThrows(BaseException.class, () -> {
			trainingController.updateTraining(trainingId, request, MOCK_TOKEN);
		});

		// Then
		assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
		verify(trainingService, times(1)).updateTraining(request, MOCK_TOKEN);
	}


	// --- deleteTrainingById Tests ---

	@Test
	void testDeleteTrainingById_Success() {
		// Given
		Long trainingId = 1L;
		// trainingService'in deleteTrainingById metodunun yeni parametreleri kabul ettiğini varsayıyoruz.
		doNothing().when(trainingService).deleteTrainingById(anyLong(), anyString());

		// When
		ResponseEntity<?> response = trainingController.deleteTrainingById(trainingId, MOCK_TOKEN);

		// Then
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertNull(response.getBody()); // DELETE için genellikle body boş olur (204 No Content)
		// Verify metodu token parametresiyle çağrıldı mı kontrol ediyoruz.
		verify(trainingService, times(1)).deleteTrainingById(trainingId, MOCK_TOKEN);
	}

	@Test
	void testDeleteTrainingById_Failure_NotFound() {
		// Given
		Long trainingId = 99L;
		ErrorMessage expectedErrorMessage = new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Training not found for ID: " + trainingId);
		// Servis, training bulunamazsa BaseException fırlatmalı
		doThrow(new BaseException(expectedErrorMessage)).when(trainingService).deleteTrainingById(anyLong(), anyString());

		// When
		BaseException exception = assertThrows(BaseException.class, () -> {
			trainingController.deleteTrainingById(trainingId, MOCK_TOKEN);
		});

		// Then
		assertEquals(expectedErrorMessage.prepareErrorMessage(), exception.getMessage());
		verify(trainingService, times(1)).deleteTrainingById(trainingId, MOCK_TOKEN);
	}
}