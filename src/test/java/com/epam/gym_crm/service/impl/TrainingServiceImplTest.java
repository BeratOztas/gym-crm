package com.epam.gym_crm.service.impl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym_crm.api.dto.request.trainee.TraineeTrainingListRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerTrainingListRequest;
import com.epam.gym_crm.api.dto.request.training.TrainingCreateRequest;
import com.epam.gym_crm.api.dto.request.training.TrainingUpdateRequest;
import com.epam.gym_crm.api.dto.response.TraineeTrainingInfoProjection;
import com.epam.gym_crm.api.dto.response.TraineeTrainingInfoResponse;
import com.epam.gym_crm.api.dto.response.TrainerTrainingInfoProjection;
import com.epam.gym_crm.api.dto.response.TrainerTrainingInfoResponse;
import com.epam.gym_crm.api.dto.response.TrainingResponse;
import com.epam.gym_crm.db.entity.Trainee;
import com.epam.gym_crm.db.entity.Trainer;
import com.epam.gym_crm.db.entity.Training;
import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.TraineeRepository;
import com.epam.gym_crm.db.repository.TrainerRepository;
import com.epam.gym_crm.db.repository.TrainingRepository;
import com.epam.gym_crm.db.repository.TrainingTypeRepository;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.service.impl.AuthenticationInfoService;
import com.epam.gym_crm.domain.service.impl.TrainingServiceImpl;
import com.epam.gym_crm.monitoring.metric.AppMetrics;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private AuthenticationInfoService authenticationInfoService;
    @Mock
    private AppMetrics appMetrics;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private User testTraineeUser;
    private Trainee testTrainee;
    private User testTrainerUser;
    private Trainer testTrainer;
    private TrainingType testTrainingType;
    private Training testTraining;

    @BeforeEach
    void setUp() {
        testTraineeUser = new User(1L, "Test", "Trainee", "test.trainee", "pass", true, null, null);
        testTrainerUser = new User(2L, "Test", "Trainer", "test.trainer", "pass", true, null, null);
        testTrainingType = new TrainingType(1L, "Yoga");
        testTrainee = new Trainee(1L, null, null, testTraineeUser, new HashSet<>(), new HashSet<>());
        testTrainer = new Trainer(1L, testTrainingType, testTrainerUser, new HashSet<>(), new HashSet<>());
        testTraining = new Training(1L, "Morning Yoga", LocalDate.now(), 60, testTrainee, testTrainer, testTrainingType);
    }

    // --- getTrainingById Tests ---
    @Test
    void shouldGetTrainingByIdSuccessfullyForAssociatedTrainee() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testTraineeUser.getUsername());
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));

        TrainingResponse response = trainingService.getTrainingById(1L);

        assertNotNull(response);
        assertEquals("Morning Yoga", response.getTrainingName());
    }

    @Test
    void shouldGetTrainingByIdSuccessfullyForAssociatedTrainer() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testTrainerUser.getUsername());
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        
        TrainingResponse response = trainingService.getTrainingById(1L);
        
        assertNotNull(response);
    }

    @Test
    void shouldThrowExceptionWhenGetTrainingByIdWithNullOrInvalidId() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        assertThrows(BaseException.class, () -> trainingService.getTrainingById(null));
        assertThrows(BaseException.class, () -> trainingService.getTrainingById(0L));
    }

    @Test
    void shouldThrowExceptionWhenGetTrainingByIdNotFound() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.getTrainingById(99L));
    }

    @Test
    void shouldThrowExceptionWhenGetTrainingByIdUnauthorized() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("unauthorized.user");
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        assertThrows(BaseException.class, () -> trainingService.getTrainingById(1L));
    }

    // --- getAllTrainings Tests ---
    @Test
    void shouldGetAllTrainingsSuccessfully() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainingRepository.findAll()).thenReturn(List.of(testTraining));
        
        List<TrainingResponse> result = trainingService.getAllTrainings();
        
        assertEquals(1, result.size());
    }

    // --- getTraineeTrainingsList Tests ---
    @Test
    void shouldGetTraineeTrainingsListSuccessfully() {
        String username = testTraineeUser.getUsername();
        TraineeTrainingListRequest request = new TraineeTrainingListRequest(null, null, null, null);
        List<TraineeTrainingInfoProjection> projections = Collections.emptyList();

        when(authenticationInfoService.getCurrentUsername()).thenReturn(username);
        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(testTrainee));
        when(trainingRepository.findTraineeTrainingsByCriteria(any(), any(), any(), any(), any())).thenReturn(projections);
        
        List<TraineeTrainingInfoResponse> result = trainingService.getTraineeTrainingsList(username, request);
        
        assertNotNull(result);
    }

    @Test
    void shouldThrowExceptionWhenGetTraineeTrainingsListUnauthorizedUser() {
        String requestedUsername = "trainee.user";
        String unauthorizedUsername = "unauthorized.user";
        TraineeTrainingListRequest request = new TraineeTrainingListRequest(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 31),
                "trainer.user",
                "TrainingType"
        );

        when(authenticationInfoService.getCurrentUsername()).thenReturn(unauthorizedUsername);

        assertThrows(BaseException.class, () -> trainingService.getTraineeTrainingsList(requestedUsername, request));

        verify(traineeRepository, never()).findByUserUsername(anyString());
        verify(trainingRepository, never()).findTraineeTrainingsByCriteria(anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenGetTraineeTrainingsListTraineeNotFound() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("non.existent");
        when(traineeRepository.findByUserUsername("non.existent")).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.getTraineeTrainingsList("non.existent", new TraineeTrainingListRequest()));
    }

    @Test
    void shouldThrowExceptionWhenGetTraineeTrainingsListTraineeNotActive() {
        testTraineeUser.setActive(false);
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testTraineeUser.getUsername());
        when(traineeRepository.findByUserUsername(testTraineeUser.getUsername())).thenReturn(Optional.of(testTrainee));
        assertThrows(BaseException.class, () -> trainingService.getTraineeTrainingsList(testTraineeUser.getUsername(), new TraineeTrainingListRequest()));
    }

    // --- getTrainerTrainingsList Tests ---
    @Test
    void shouldGetTrainerTrainingsListSuccessfully() {
        String username = testTrainerUser.getUsername();
        TrainerTrainingListRequest request = new TrainerTrainingListRequest(null, null, null);
        List<TrainerTrainingInfoProjection> projections = Collections.emptyList();

        when(authenticationInfoService.getCurrentUsername()).thenReturn(username);
        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(testTrainer));
        when(trainingRepository.findTrainerTrainingsByCriteria(any(), any(), any(), any())).thenReturn(projections);
        
        List<TrainerTrainingInfoResponse> result = trainingService.getTrainerTrainingsList(username, request);
        
        assertNotNull(result);
    }

    @Test
    void shouldThrowExceptionWhenGetTrainerTrainingsListUnauthorizedUser() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        assertThrows(BaseException.class, () -> trainingService.getTrainerTrainingsList("another.user", new TrainerTrainingListRequest()));
    }

    @Test
    void shouldThrowExceptionWhenGetTrainerTrainingsListTrainerNotFound() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("non.existent");
        when(trainerRepository.findByUserUsername("non.existent")).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.getTrainerTrainingsList("non.existent", new TrainerTrainingListRequest()));
    }

    // --- createTraining Tests ---
    @Test
    void shouldCreateTrainingSuccessfully() {
        TrainingCreateRequest request = new TrainingCreateRequest(testTraineeUser.getUsername(), testTrainerUser.getUsername(), "Yoga", LocalDate.now(), 60);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.of(testTrainer));
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.of(testTrainee));
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(anyString())).thenReturn(Optional.of(testTrainingType));
        when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

        TrainingResponse response = trainingService.createTraining(request);
        
        assertNotNull(response);
        verify(appMetrics).incrementTrainingCreation();
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainingTrainerNotFound() {
        TrainingCreateRequest request = new TrainingCreateRequest(null, "non.existent", null, null, 0);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername("non.existent")).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.createTraining(request));
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainingTrainerNotActive() {
        testTrainerUser.setActive(false);
        TrainingCreateRequest request = new TrainingCreateRequest(null, testTrainerUser.getUsername(), null, null, 0);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername(testTrainerUser.getUsername())).thenReturn(Optional.of(testTrainer));
        assertThrows(BaseException.class, () -> trainingService.createTraining(request));
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainingTraineeNotFound() {
        TrainingCreateRequest request = new TrainingCreateRequest("non.existent", testTrainerUser.getUsername(), null, null, 0);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.of(testTrainer));
        when(traineeRepository.findByUserUsername("non.existent")).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.createTraining(request));
    }
    
    @Test
    void shouldThrowExceptionWhenCreateTrainingTraineeNotActive() {
        testTraineeUser.setActive(false);
        TrainingCreateRequest request = new TrainingCreateRequest(testTraineeUser.getUsername(), testTrainerUser.getUsername(), null, null, 0);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.of(testTrainer));
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.of(testTrainee));
        assertThrows(BaseException.class, () -> trainingService.createTraining(request));
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainingTrainingTypeNotFound() {
        TrainingCreateRequest request = new TrainingCreateRequest(testTraineeUser.getUsername(), testTrainerUser.getUsername(), "NonExistent", null, 0);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername(anyString())).thenReturn(Optional.of(testTrainer));
        when(traineeRepository.findByUserUsername(anyString())).thenReturn(Optional.of(testTrainee));
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.createTraining(request));
    }

    // --- updateTraining Tests ---
    @Test
    void shouldUpdateTrainingSuccessfully() {
        TrainingUpdateRequest request = new TrainingUpdateRequest(1L, "New Name", null, 0, null, null, null);
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testTraineeUser.getUsername());
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        when(trainingRepository.save(any(Training.class))).thenReturn(testTraining);

        TrainingResponse response = trainingService.updateTraining(request);
        
        assertEquals("New Name", response.getTrainingName());
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainingWithNullOrInvalidId() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        assertThrows(BaseException.class, () -> trainingService.updateTraining(new TrainingUpdateRequest(null, null, null, 0, null, null, null)));
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainingNotFound() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.updateTraining(new TrainingUpdateRequest(99L, null, null, 0, null, null, null)));
    }
    
    @Test
    void shouldThrowExceptionWhenUpdateTrainingUnauthorized() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("unauthorized.user");
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        assertThrows(BaseException.class, () -> trainingService.updateTraining(new TrainingUpdateRequest(1L, null, null, 0, null, null, null)));
    }
    
    @Test
    void shouldThrowExceptionWhenUpdateTrainingNewTrainerNotFound() {
        TrainingUpdateRequest request = new TrainingUpdateRequest(1L, null, null, 0, "non.existent", null, null);
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testTraineeUser.getUsername());
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        when(trainerRepository.findByUserUsername("non.existent")).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.updateTraining(request));
    }
    
    @Test
    void shouldThrowExceptionWhenUpdateTrainingNewTrainerNotActive() {
        testTrainerUser.setActive(false);
        TrainingUpdateRequest request = new TrainingUpdateRequest(1L, null, null, 0, testTrainerUser.getUsername(), null, null);
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testTraineeUser.getUsername());
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        when(trainerRepository.findByUserUsername(testTrainerUser.getUsername())).thenReturn(Optional.of(testTrainer));
        assertThrows(BaseException.class, () -> trainingService.updateTraining(request));
    }
    
    @Test
    void shouldThrowExceptionWhenUpdateTrainingNewTraineeNotFound() {
        TrainingUpdateRequest request = new TrainingUpdateRequest(1L, null, null, 0, null, "non.existent", null);
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testTrainerUser.getUsername());
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        when(traineeRepository.findByUserUsername("non.existent")).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.updateTraining(request));
    }
    
    @Test
    void shouldThrowExceptionWhenUpdateTrainingNewTraineeNotActive() {
        testTraineeUser.setActive(false);
        TrainingUpdateRequest request = new TrainingUpdateRequest(1L, null, null, 0, null, testTraineeUser.getUsername(), null);
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testTrainerUser.getUsername());
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        when(traineeRepository.findByUserUsername(testTraineeUser.getUsername())).thenReturn(Optional.of(testTrainee));
        assertThrows(BaseException.class, () -> trainingService.updateTraining(request));
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainingNewTrainingTypeNotFound() {
        TrainingUpdateRequest request = new TrainingUpdateRequest(1L, null, null, 0, null, null, "NonExistent");
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testTraineeUser.getUsername());
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.updateTraining(request));
    }

    // --- deleteTrainingById Tests ---
    @Test
    void shouldDeleteTrainingByIdSuccessfully() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testTraineeUser.getUsername());
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        
        trainingService.deleteTrainingById(1L);
        
        verify(trainingRepository).delete(testTraining);
    }
    
    @Test
    void shouldThrowExceptionWhenDeleteTrainingByIdWithNullOrInvalidId() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        assertThrows(BaseException.class, () -> trainingService.deleteTrainingById(null));
        assertThrows(BaseException.class, () -> trainingService.deleteTrainingById(0L));
    }
    
    @Test
    void shouldThrowExceptionWhenDeleteTrainingByIdNotFound() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainingService.deleteTrainingById(99L));
    }
    
    @Test
    void shouldThrowExceptionWhenDeleteTrainingByIdUnauthorized() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("unauthorized.user");
        when(trainingRepository.findById(1L)).thenReturn(Optional.of(testTraining));
        assertThrows(BaseException.class, () -> trainingService.deleteTrainingById(1L));
    }
}