package com.epam.gym_crm.service.impl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym_crm.api.dto.UserCreationResult;
import com.epam.gym_crm.api.dto.request.UserActivationRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerCreateRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerUpdateRequest;
import com.epam.gym_crm.api.dto.response.TrainerInfoResponse;
import com.epam.gym_crm.api.dto.response.TrainerProfileResponse;
import com.epam.gym_crm.api.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.db.entity.Trainee;
import com.epam.gym_crm.db.entity.Trainer;
import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.TraineeRepository;
import com.epam.gym_crm.db.repository.TrainerRepository;
import com.epam.gym_crm.db.repository.TrainingRepository;
import com.epam.gym_crm.db.repository.TrainingTypeRepository;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.service.IAuthenticationService;
import com.epam.gym_crm.domain.service.impl.AuthenticationInfoService;
import com.epam.gym_crm.domain.service.impl.TrainerServiceImpl;
import com.epam.gym_crm.monitoring.metric.AppMetrics;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private IAuthenticationService authenticationService;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationInfoService authenticationInfoService; 
    @Mock
    private AppMetrics appMetrics;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private User testUser;
    private Trainer testTrainer;
    private TrainingType testSpecialization;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "John", "Doe", "John.Doe", "pass", true, null, null);
        testSpecialization = new TrainingType(1L, "Fitness");
        testTrainer = new Trainer(1L, testSpecialization, testUser, new HashSet<>(), new HashSet<>());
    }

    // --- findTrainerById Tests ---
    @Test
    void shouldFindTrainerByIdSuccessfully() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(testTrainer));
        
        TrainerProfileResponse response = trainerService.findTrainerById(1L);
        
        assertNotNull(response);
        assertEquals(testUser.getUsername(), response.getUsername());
    }

    @Test
    void shouldThrowExceptionWhenFindTrainerByIdWithNullOrInvalidId() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        assertThrows(BaseException.class, () -> trainerService.findTrainerById(0L));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void shouldThrowExceptionWhenFindTrainerByIdNotFound() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainerService.findTrainerById(99L));
    }

    // --- findTrainerByUsername Tests ---
    @Test
    void shouldFindTrainerByUsernameSuccessfully() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(testTrainer));
        
        TrainerProfileResponse response = trainerService.findTrainerByUsername("John.Doe");
        
        assertNotNull(response);
        assertEquals("John.Doe", response.getUsername());
    }

    @Test
    void shouldThrowExceptionWhenFindTrainerByUsernameWithNullOrEmpty() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        assertThrows(BaseException.class, () -> trainerService.findTrainerByUsername(null));
        assertThrows(BaseException.class, () -> trainerService.findTrainerByUsername(""));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void shouldThrowExceptionWhenFindTrainerByUsernameNotFound() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername("non.existent")).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainerService.findTrainerByUsername("non.existent"));
    }

    // --- getAllTrainers Tests ---
    @Test
    void shouldGetAllTrainersSuccessfully() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findAll()).thenReturn(List.of(testTrainer));
        
        List<TrainerProfileResponse> responseList = trainerService.getAllTrainers();
        
        assertEquals(1, responseList.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoTrainersExist() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findAll()).thenReturn(Collections.emptyList());
        
        List<TrainerProfileResponse> responseList = trainerService.getAllTrainers();
        
        assertTrue(responseList.isEmpty());
    }

    // --- getUnassignedTrainersForTrainee Tests ---
    @Test
    void shouldGetUnassignedTrainersForTraineeSuccessfully() {
        User traineeUser = new User(2L, "Test", "Trainee", "Test.Trainee", "p", true, null, null);
        Trainee trainee = new Trainee(2L, null, null, traineeUser, new HashSet<>(Set.of(testTrainer)), null);
        
        Trainer unassignedTrainer = new Trainer(3L, testSpecialization, new User(3L,"Unassigned","Trainer","Unassigned.Trainer","p",true,null,null),null,null);

        when(authenticationInfoService.getCurrentUsername()).thenReturn("Test.Trainee");
        when(traineeRepository.findByUserUsername("Test.Trainee")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserIsActive(true)).thenReturn(List.of(testTrainer, unassignedTrainer));
        
        List<TrainerInfoResponse> result = trainerService.getUnassignedTrainersForTrainee("Test.Trainee");

        assertEquals(1, result.size());
        assertEquals("Unassigned.Trainer", result.get(0).getUsername());
    }

    @Test
    void shouldThrowExceptionWhenGetUnassignedTrainersForInactiveTrainee() {
        testUser.setActive(false); 
        when(authenticationInfoService.getCurrentUsername()).thenReturn(testUser.getUsername());
        when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(new Trainee(1L,null,null,testUser,null,null)));

        assertThrows(BaseException.class, () -> trainerService.getUnassignedTrainersForTrainee(testUser.getUsername()));
        verify(trainerRepository, never()).findByUserIsActive(anyBoolean());
    }

    // --- createTrainer Tests ---
    @Test
    void shouldCreateTrainerSuccessfully() {
        TrainerCreateRequest request = new TrainerCreateRequest("Jane", "Doe", "Fitness");
        User newUser = new User(2L, "Jane", "Doe", "Jane.Doe", "encoded", true, null, null);
        UserCreationResult creationResult = new UserCreationResult(newUser, "rawPassword");
        
        when(authenticationService.prepareUserWithCredentials(anyString(), anyString())).thenReturn(creationResult);
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase("Fitness")).thenReturn(Optional.of(testSpecialization));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(new Trainer(2L,testSpecialization, newUser,null,null));
        when(authenticationService.createAccessToken(newUser)).thenReturn("dummy.token");

        UserRegistrationResponse response = trainerService.createTrainer(request);
        
        assertNotNull(response);
        assertEquals("Jane.Doe", response.getUsername());
        assertEquals("rawPassword", response.getPassword());
        verify(appMetrics).incrementTrainerCreation();
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainerRequestIsNull() {
        assertThrows(BaseException.class, () -> trainerService.createTrainer(null));
        verifyNoInteractions(authenticationService, trainingTypeRepository, trainerRepository);
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainerTrainingTypeNotFound() {
        TrainerCreateRequest request = new TrainerCreateRequest("Jane", "Doe", "NonExistent");
        User newUser = new User(2L, "Jane", "Doe", "Jane.Doe", "encoded", true, null, null);
        UserCreationResult creationResult = new UserCreationResult(newUser, "rawPassword");
        
        when(authenticationService.prepareUserWithCredentials(anyString(), anyString())).thenReturn(creationResult);
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> trainerService.createTrainer(request));
        verify(trainerRepository, never()).save(any());
    }

    // --- updateTrainer Tests ---
    @Test
    void shouldUpdateTrainerSuccessfully() {
    	
        String username = "trainer.user";
        String newFirstName = "Johnny";
        String newLastName = "Depp";
        String newSpecialization = "History";

        TrainerUpdateRequest updateRequest = new TrainerUpdateRequest();
        updateRequest.setUsername(username);
        updateRequest.setFirstName(newFirstName);
        updateRequest.setLastName(newLastName);
        updateRequest.setSpecialization(newSpecialization);

        User user = new User(1L, "Old", "User", username, "password", true, null, null);
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        TrainingType specialization = new TrainingType(2L, newSpecialization);
        trainer.setSpecialization(specialization);

        when(authenticationInfoService.getCurrentUsername()).thenReturn(username);
        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(newSpecialization)).thenReturn(Optional.of(specialization));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        TrainerProfileResponse response = trainerService.updateTrainer(updateRequest);

        assertNotNull(response);
        assertEquals(username, response.getUsername());
        assertEquals(newFirstName, response.getFirstName());
        assertEquals(newLastName, response.getLastName());
        assertEquals(newSpecialization, response.getSpecialization());
        
        verify(trainerRepository, times(1)).findByUserUsername(username);
        verify(trainingTypeRepository, times(1)).findByTrainingTypeNameIgnoreCase(newSpecialization);
        verify(trainerRepository, times(1)).save(any(Trainer.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainerRequestIsNull() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        assertThrows(BaseException.class, () -> trainerService.updateTrainer(null));
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainerUnauthorized() {
        TrainerUpdateRequest request = new TrainerUpdateRequest("John.Doe", null, null, null, true);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("unauthorized.user");
        assertThrows(BaseException.class, () -> trainerService.updateTrainer(request));
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainerNotFound() {
        TrainerUpdateRequest request = new TrainerUpdateRequest("non.existent", null, null, null, true);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("non.existent");
        when(trainerRepository.findByUserUsername("non.existent")).thenReturn(Optional.empty());
        assertThrows(BaseException.class, () -> trainerService.updateTrainer(request));
    }
    
    @Test
    void shouldThrowExceptionWhenUpdateTrainerTrainingTypeNotFound() {
        TrainerUpdateRequest request = new TrainerUpdateRequest("John.Doe", null, null, "NonExistent", true);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("John.Doe");
        when(trainerRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(testTrainer));
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> trainerService.updateTrainer(request));
        verify(trainerRepository, never()).save(any());
    }

    // --- activateDeactivateTrainer Tests ---
    @Test
    void shouldDeactivateTrainerSuccessfully() {
        UserActivationRequest request = new UserActivationRequest("John.Doe", false);
        testUser.setActive(true);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(testTrainer));
        
        trainerService.activateDeactivateTrainer(request);
        
        assertFalse(testUser.isActive());
        verify(userRepository).save(testUser);
        verify(trainerRepository).save(testTrainer);
    }
    
    @Test
    void shouldActivateTrainerSuccessfully() {
        UserActivationRequest request = new UserActivationRequest("John.Doe", true);
        testUser.setActive(false);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(testTrainer));
        
        trainerService.activateDeactivateTrainer(request);
        
        assertTrue(testUser.isActive());
    }

    @Test
    void shouldDoNothingWhenTrainerAlreadyInDesiredState() {
        UserActivationRequest request = new UserActivationRequest("John.Doe", true);
        testUser.setActive(true);
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        when(trainerRepository.findByUserUsername("John.Doe")).thenReturn(Optional.of(testTrainer));
        
        trainerService.activateDeactivateTrainer(request);
        
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenActivateDeactivateRequestIsNull() {
        when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
        assertThrows(NullPointerException.class, () -> trainerService.activateDeactivateTrainer(null));
    }
}