package com.epam.gym_crm.controller.impl;

import com.epam.gym_crm.controller.ApiResponse;
import com.epam.gym_crm.model.TrainingType;
import com.epam.gym_crm.service.ITrainingTypeService;
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
class RestTrainingTypeControllerImplTest {

    @Mock
    private ITrainingTypeService trainingTypeService;

    @InjectMocks
    private RestTrainingTypeControllerImpl trainingTypeController;

    @Test
    void testGetTrainingTypes_Success() {
        // Servisin, içinde bir TrainingType nesnesi olan bir liste döndürmesini simüle et
        TrainingType cardio = new TrainingType(1L, "Cardio");
        List<TrainingType> trainingTypes = Collections.singletonList(cardio);
        
        when(trainingTypeService.getTrainingTypes()).thenReturn(trainingTypes);

        // Act: Test edilecek metodu çağır
        ResponseEntity<ApiResponse<List<TrainingType>>> response = trainingTypeController.getTrainingTypes();

        // Assert: Sonuçları doğrula
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Dönen payload'un, servisten dönmesini beklediğimiz liste ile aynı olduğunu kontrol et
        assertEquals(trainingTypes, response.getBody().getPayload());
        
        // Listenin boş olmadığını ve bir eleman içerdiğini kontrol et
        assertFalse(response.getBody().getPayload().isEmpty());
        assertEquals(1, response.getBody().getPayload().size());
        assertEquals("Cardio", response.getBody().getPayload().get(0).getTrainingTypeName());

        // Servisin getTrainingTypes metodunun 1 kez çağrıldığını doğrula
        verify(trainingTypeService, times(1)).getTrainingTypes();
    }

    @Test
    void testGetTrainingTypes_Success_EmptyList() {
    	
        List<TrainingType> emptyList = Collections.emptyList();
        when(trainingTypeService.getTrainingTypes()).thenReturn(emptyList);

        // Act
        ResponseEntity<ApiResponse<List<TrainingType>>> response = trainingTypeController.getTrainingTypes();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Dönen payload'un boş bir liste olduğunu doğrula
        assertTrue(response.getBody().getPayload().isEmpty());
        
        // Servisin 1 kez çağrıldığını doğrula
        verify(trainingTypeService, times(1)).getTrainingTypes();
    }
}