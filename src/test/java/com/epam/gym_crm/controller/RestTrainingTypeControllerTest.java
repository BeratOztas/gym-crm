package com.epam.gym_crm.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.epam.gym_crm.api.controller.RestTrainingTypeController;
import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.domain.service.ITrainingTypeService;

@ExtendWith(MockitoExtension.class)
class RestTrainingTypeControllerTest {

	@Mock
	private ITrainingTypeService trainingTypeService;

	@InjectMocks
	private RestTrainingTypeController trainingTypeController;

	@Test
	void testGetTrainingTypes_Success() {
        TrainingType cardio = new TrainingType(1L, "Cardio");
        List<TrainingType> trainingTypes = Collections.singletonList(cardio);
        
        when(trainingTypeService.getTrainingTypes()).thenReturn(trainingTypes);

        ResponseEntity<List<TrainingType>> response = trainingTypeController.getTrainingTypes();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        assertEquals(trainingTypes, response.getBody());
        
        assertFalse(response.getBody().isEmpty());
        assertEquals(1, response.getBody().size());
        assertEquals("Cardio", response.getBody().get(0).getTrainingTypeName());

        verify(trainingTypeService, times(1)).getTrainingTypes();
    }

	@Test
	void testGetTrainingTypes_Success_EmptyList() {

		List<TrainingType> emptyList = Collections.emptyList();
		when(trainingTypeService.getTrainingTypes()).thenReturn(emptyList);

		ResponseEntity<List<TrainingType>> response = trainingTypeController.getTrainingTypes();

		// Assert
		assertNotNull(response);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());

		assertTrue(response.getBody().isEmpty());

		verify(trainingTypeService, times(1)).getTrainingTypes();
	}
}