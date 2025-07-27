package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym_crm.auth.AuthManager;
import com.epam.gym_crm.dto.request.trainee.TraineeTrainingListRequest;
import com.epam.gym_crm.dto.request.trainer.TrainerTrainingListRequest;
import com.epam.gym_crm.dto.request.training.TrainingCreateRequest;
import com.epam.gym_crm.dto.request.training.TrainingUpdateRequest;
import com.epam.gym_crm.dto.response.TraineeTrainingInfoResponse;
import com.epam.gym_crm.dto.response.TrainerTrainingInfoResponse;
import com.epam.gym_crm.dto.response.TrainingResponse;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.model.TrainingType;
import com.epam.gym_crm.model.User;
import com.epam.gym_crm.repository.TraineeRepository;
import com.epam.gym_crm.repository.TrainerRepository;
import com.epam.gym_crm.repository.TrainingRepository;
import com.epam.gym_crm.repository.TrainingTypeRepository;
import com.epam.gym_crm.repository.UserRepository;

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
    private AuthManager authManager;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private User testUser;
    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType testTrainingType;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "user", "User", "test.user", "pass123", true, null, null);

        when(authManager.getCurrentUser()).thenReturn(testUser);

        User traineeUser = new User(2L, "Trainee", "User", "trainee.user", "traineePass", true, null, null);
        testTrainee = new Trainee(3L, LocalDate.of(2000, 1, 1), "Some Address", traineeUser, new HashSet<>(), new HashSet<>());

        User trainerUser = new User(4L, "Trainer", "User", "trainer.user", "trainerPass", true, null, null);
        testTrainingType = new TrainingType(5L, "Yoga");
        
        testTrainer = new Trainer(6L, testTrainingType, trainerUser, new HashSet<>(), new HashSet<>());
    }

    // --- 16. Add training.  ---

    @Test
    void shouldCreateTrainingSuccessfully() {
        // Given
        TrainingCreateRequest request = new TrainingCreateRequest(
        		testTrainee.getUser().getUsername(),
        		testTrainer.getUser().getUsername(),
        		testTrainingType.getTrainingTypeName(),
            LocalDate.of(2023, 10, 26),
            60
        );

        when(trainerRepository.findByUserUsername(request.getTrainerUsername()))
            .thenReturn(Optional.of(testTrainer));
        when(traineeRepository.findByUserUsername(request.getTraineeUsername()))
            .thenReturn(Optional.of(testTrainee));
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(request.getTrainingName()))
            .thenReturn(Optional.of(testTrainingType));

        
        Training savedTraining = new Training(
            10L, 
            request.getTrainingName(),
            request.getTrainingDate(),
            request.getTrainingDuration(),
            testTrainee,
            testTrainer,
            testTrainingType
        );
        when(trainingRepository.save(any(Training.class))).thenReturn(savedTraining);

        // When
        TrainingResponse response = trainingService.createTraining(request);

        // Then
        assertNotNull(response);
        assertEquals(savedTraining.getTrainingName(), response.getTrainingName());
        // assertEquals(savedTraining.getId(), response.getId()); // TrainingResponse'da ID olmadığı için kaldırıldı
        assertEquals(testTrainer.getUser().getUsername(), response.getTrainerUsername());
        assertEquals(testTrainee.getUser().getUsername(), response.getTraineeUsername());
        assertEquals(testTrainingType.getTrainingTypeName(), response.getTrainingTypeName());

        verify(authManager).getCurrentUser();
        verify(trainerRepository).findByUserUsername(request.getTrainerUsername());
        verify(traineeRepository).findByUserUsername(request.getTraineeUsername());
        verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(request.getTrainingName());
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainingTrainerNotFound() {
        // Given
        TrainingCreateRequest request = new TrainingCreateRequest(
        		testTrainee.getUser().getUsername(),
        		"nonexistent.trainer", // Non-existent trainer username
        		testTrainingType.getTrainingTypeName(),
            LocalDate.of(2023, 10, 26),
            60
        );

        when(trainerRepository.findByUserUsername(request.getTrainerUsername()))
            .thenReturn(Optional.empty()); // Trainer not found

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.createTraining(request));

        // Then
        assertEquals("Resource not found. : Trainer with username nonexistent.trainer not found.", exception.getMessage());

        verify(authManager).getCurrentUser();
        verify(trainerRepository).findByUserUsername(request.getTrainerUsername());
        verifyNoInteractions(traineeRepository); 
        verifyNoInteractions(trainingTypeRepository); 
        verifyNoInteractions(trainingRepository); 
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainingTrainerNotActive() {
        // Given
        User inactiveTrainerUser = new User(4L, "Inactive", "Trainer", "inactive.trainer", "pass", false, null, null);
        
        Trainer inactiveTrainer = new Trainer(6L, testTrainingType, inactiveTrainerUser, new HashSet<>(), new HashSet<>());

        TrainingCreateRequest request = new TrainingCreateRequest(
        		testTrainee.getUser().getUsername(),
            inactiveTrainer.getUser().getUsername(),
            testTrainingType.getTrainingTypeName(),
            LocalDate.of(2023, 10, 26),
            60
        );

        when(trainerRepository.findByUserUsername(request.getTrainerUsername()))
            .thenReturn(Optional.of(inactiveTrainer));

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.createTraining(request));

        // Then
        assertEquals("User is not active : Trainer inactive.trainer is not active. Cannot create training.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verify(trainerRepository).findByUserUsername(request.getTrainerUsername());
        verifyNoInteractions(traineeRepository);
        verifyNoInteractions(trainingTypeRepository);
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainingTraineeNotFound() {
        // Given
        TrainingCreateRequest request = new TrainingCreateRequest(
        		"nonexistent.trainee", // Non-existent trainee username
            testTrainer.getUser().getUsername(),
            testTrainingType.getTrainingTypeName(),
            LocalDate.of(2023, 10, 26),
            60
        );

        when(trainerRepository.findByUserUsername(request.getTrainerUsername()))
            .thenReturn(Optional.of(testTrainer));
        when(traineeRepository.findByUserUsername(request.getTraineeUsername()))
            .thenReturn(Optional.empty()); // Trainee not found

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.createTraining(request));

        // Then
        assertEquals("Resource not found. : Trainee with username nonexistent.trainee not found.", exception.getMessage());

        verify(authManager).getCurrentUser();
        verify(trainerRepository).findByUserUsername(request.getTrainerUsername());
        verify(traineeRepository).findByUserUsername(request.getTraineeUsername());
        verifyNoInteractions(trainingTypeRepository);
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainingTraineeNotActive() {
        // Given
        User inactiveTraineeUser = new User(2L, "Inactive", "Trainee", "inactive.trainee", "pass", false, null, null);
        
        Trainee inactiveTrainee = new Trainee(3L, LocalDate.of(2000, 1, 1), "Some Address", inactiveTraineeUser, new HashSet<>(), new HashSet<>());

        TrainingCreateRequest request = new TrainingCreateRequest(
        		inactiveTrainee.getUser().getUsername(),
            testTrainer.getUser().getUsername(),
            testTrainingType.getTrainingTypeName(),
            LocalDate.of(2023, 10, 26),
            60
        );

        when(trainerRepository.findByUserUsername(request.getTrainerUsername()))
            .thenReturn(Optional.of(testTrainer));
        when(traineeRepository.findByUserUsername(request.getTraineeUsername()))
            .thenReturn(Optional.of(inactiveTrainee));

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.createTraining(request));

        // Then
        assertEquals("User is not active : Trainee inactive.trainee is not active. Cannot create training.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verify(trainerRepository).findByUserUsername(request.getTrainerUsername());
        verify(traineeRepository).findByUserUsername(request.getTraineeUsername());
        verifyNoInteractions(trainingTypeRepository);
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldThrowExceptionWhenCreateTrainingTrainingTypeNotFound() {
        // Given
        TrainingCreateRequest request = new TrainingCreateRequest(
        		testTrainee.getUser().getUsername(),
        		testTrainer.getUser().getUsername(),
        		"NonExistentType" ,// Non-existent training type
            LocalDate.of(2023, 10, 26),
            60
        );

        when(trainerRepository.findByUserUsername(request.getTrainerUsername()))
            .thenReturn(Optional.of(testTrainer));
        when(traineeRepository.findByUserUsername(request.getTraineeUsername()))
            .thenReturn(Optional.of(testTrainee));
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(request.getTrainingName()))
            .thenReturn(Optional.empty()); // Training type not found

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.createTraining(request));

        // Then
        assertEquals("Resource not found. : Training Type NonExistentType not found.", exception.getMessage());

        verify(authManager).getCurrentUser();
        verify(trainerRepository).findByUserUsername(request.getTrainerUsername());
        verify(traineeRepository).findByUserUsername(request.getTraineeUsername());
        verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(request.getTrainingName());
        verifyNoInteractions(trainingRepository);
    }

    // --- 14. Get Trainee Trainings List  ---

    @Test
    void shouldGetTraineeTrainingsListSuccessfully() {
        // Given
        TraineeTrainingListRequest request = new TraineeTrainingListRequest(
            testTrainee.getUser().getUsername(),
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 12, 31),
            testTrainer.getUser().getFirstName(),
            testTrainingType.getTrainingTypeName()
        );

        when(authManager.getCurrentUser()).thenReturn(testTrainee.getUser());

        // NOT: Training nesnelerinden doğrudan TraineeTrainingInfoResponse nesneleri oluşturmalısınız.
        // TraineeTrainingInfoResponse'un constructor'ı, trainerName'i doğrudan String olarak alıyor.
        // Eğer TraineeTrainingInfoResponse'un constructor'ı Trainer nesnesi alıyorsa, ona göre ayarlayın.
        // Varsayım: TraineeTrainingInfoResponse(String trainingName, LocalDate trainingDate, String trainingType, Integer trainingDuration, String trainerName)
        TraineeTrainingInfoResponse trainingInfo1 = new TraineeTrainingInfoResponse(
            "Yoga",
            LocalDate.of(2023, 5, 10),
            testTrainingType.getTrainingTypeName(), // TrainingType nesnesinden name'i alın
            60,
            testTrainer.getUser().getFirstName() + " " + testTrainer.getUser().getLastName() // Trainer'ın tam adını verin
        );
        TraineeTrainingInfoResponse trainingInfo2 = new TraineeTrainingInfoResponse(
            "Pilates",
            LocalDate.of(2023, 6, 15),
            testTrainingType.getTrainingTypeName(), // TrainingType nesnesinden name'i alın
            45,
            testTrainer.getUser().getFirstName() + " " + testTrainer.getUser().getLastName() // Trainer'ın tam adını verin
        );

        List<TraineeTrainingInfoResponse> mockResponseList = Arrays.asList(
            trainingInfo1,
            trainingInfo2
        );

        when(traineeRepository.findByUserUsername(request.getTraineeUsername()))
            .thenReturn(Optional.of(testTrainee));

        // Burası düzeltildi: now returns List<TraineeTrainingInfoResponse>
        when(trainingRepository.findTraineeTrainingsByCriteria(
                request.getTraineeUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTrainerName(), // Bu parametrenin Trainer'ın tam adı mı yoksa sadece adı mı olması gerektiğinden emin olun.
                request.getTrainingTypeName()))
            .thenReturn(mockResponseList); // Artık doğru tipte mocklanıyor

        // When
        List<TraineeTrainingInfoResponse> result = trainingService.getTraineeTrainingsList(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(mockResponseList.get(0).getTrainingName(), result.get(0).getTrainingName());
        assertEquals(mockResponseList.get(1).getTrainingName(), result.get(1).getTrainingName());
        assertEquals(mockResponseList.get(0).getTrainerName(), result.get(0).getTrainerName()); // TrainerName'i de kontrol edebilirsiniz
        assertEquals(mockResponseList.get(1).getTrainerName(), result.get(1).getTrainerName());


        verify(authManager).getCurrentUser();
        verify(traineeRepository).findByUserUsername(request.getTraineeUsername());
        verify(trainingRepository).findTraineeTrainingsByCriteria(
                request.getTraineeUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTrainerName(),
                request.getTrainingTypeName());
    }

    @Test
    void shouldThrowExceptionWhenGetTraineeTrainingsListUnauthorizedUser() {
        // Given
        TraineeTrainingListRequest request = new TraineeTrainingListRequest(
            testTrainee.getUser().getUsername(), // Request for a different trainee
            null, null, null, null
        );

        // Current user is NOT the requested trainee
        User otherUser = new User(5L, "Other", "User", "other.user", "pass", true, null, null);
        when(authManager.getCurrentUser()).thenReturn(otherUser);

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.getTraineeTrainingsList(request));

        // Then
        assertEquals("User authentication required : You are not authorized to view trainings for other trainees.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verifyNoInteractions(traineeRepository);
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldThrowExceptionWhenGetTraineeTrainingsListTraineeNotFound() {
        // Given
        TraineeTrainingListRequest request = new TraineeTrainingListRequest(
            "nonexistent.trainee", 
            null, null, null, null
        );

        // Current user is the one specified in the request
        User nonexistentTraineeUser = new User(7L, "NonEx", "Trainee", "nonexistent.trainee", "pass", true, null, null);
        when(authManager.getCurrentUser()).thenReturn(nonexistentTraineeUser);

        when(traineeRepository.findByUserUsername(request.getTraineeUsername()))
            .thenReturn(Optional.empty()); // Trainee not found

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.getTraineeTrainingsList(request));

        // Then
        assertEquals("Resource not found. : Trainee with username nonexistent.trainee not found.", exception.getMessage());

        verify(authManager).getCurrentUser();
        verify(traineeRepository).findByUserUsername(request.getTraineeUsername());
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldThrowExceptionWhenGetTraineeTrainingsListTraineeNotActive() {
        // Given
        User inactiveTraineeUser = new User(2L, "Inactive", "Trainee", "inactive.trainee", "pass", false, null, null);
        Trainee inactiveTrainee = new Trainee(3L, LocalDate.of(2000, 1, 1), "Some Address", inactiveTraineeUser, new HashSet<>(), new HashSet<>());

        TraineeTrainingListRequest request = new TraineeTrainingListRequest(
            inactiveTrainee.getUser().getUsername(),
            null, null, null, null
        );

        // Current user is the inactive trainee
        when(authManager.getCurrentUser()).thenReturn(inactiveTrainee.getUser());

        when(traineeRepository.findByUserUsername(request.getTraineeUsername()))
            .thenReturn(Optional.of(inactiveTrainee));

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.getTraineeTrainingsList(request));

        // Then
        assertEquals("User is not active : Trainee inactive.trainee is not active. Cannot retrieve their trainings.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verify(traineeRepository).findByUserUsername(request.getTraineeUsername());
        verifyNoInteractions(trainingRepository);
    }

    // --- 15. Get Trainer Trainings List ---

    @Test
    void shouldGetTrainerTrainingsListSuccessfully() {
        // Given
        // NOT: TrainerTrainingListRequest artık trainingTypeName'ı almıyor olmalı.
        // Eğer alıyorsa ve kullanmıyorsa, parametreyi silin veya null geçin.
        // Önceki commit mesajınızda trainer tarafında TrainingTypeName filtresinin kaldırıldığı belirtilmişti.
        TrainerTrainingListRequest request = new TrainerTrainingListRequest(
            testTrainer.getUser().getUsername(),
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 12, 31),
            testTrainee.getUser().getFirstName() // Eğer bu TrainerTrainingListRequest trainerName yerine traineeName alıyorsa.
                                               // Veya TrainerTrainingListRequest'in constructor'ını kontrol edin.
        );

        // Ensure current user is the requested trainer
        when(authManager.getCurrentUser()).thenReturn(testTrainer.getUser());

        // NOT: TrainerTrainingInfoResponse nesneleri oluşturmalısınız.
        // TrainerTrainingInfoResponse constructor: trainingName, trainingDate, trainingType, trainingDuration, traineeName
        TrainerTrainingInfoResponse trainingInfo1 = new TrainerTrainingInfoResponse(
            "Strength",
            LocalDate.of(2023, 5, 10),
            testTrainingType.getTrainingTypeName(), // TrainingType adını verin
            60,
            testTrainee.getUser().getFirstName() + " " + testTrainee.getUser().getLastName() // Trainee'nin tam adını verin
        );
        TrainerTrainingInfoResponse trainingInfo2 = new TrainerTrainingInfoResponse(
            "Cardio",
            LocalDate.of(2023, 6, 15),
            testTrainingType.getTrainingTypeName(), // TrainingType adını verin
            45,
            testTrainee.getUser().getFirstName() + " " + testTrainee.getUser().getLastName() // Trainee'nin tam adını verin
        );

        List<TrainerTrainingInfoResponse> mockResponseList = Arrays.asList(
            trainingInfo1,
            trainingInfo2
        );

        when(trainerRepository.findByUserUsername(request.getTrainerUsername()))
            .thenReturn(Optional.of(testTrainer));

        // Burası düzeltildi: now returns List<TrainerTrainingInfoResponse>
        // Ayrıca, trainingRepository.findTrainerTrainingsByCriteria artık trainingTypeName parametresi almamalı.
        when(trainingRepository.findTrainerTrainingsByCriteria(
                request.getTrainerUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTraineeName()
                ))
            .thenReturn(mockResponseList); // Artık doğru tipte mocklanıyor

        // When
        List<TrainerTrainingInfoResponse> result = trainingService.getTrainerTrainingsList(request);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(mockResponseList.get(0).getTrainingName(), result.get(0).getTrainingName());
        assertEquals(mockResponseList.get(1).getTrainingName(), result.get(1).getTrainingName());
        assertEquals(mockResponseList.get(0).getTraineeName(), result.get(0).getTraineeName()); // TraineeName'i de kontrol edebilirsiniz
        assertEquals(mockResponseList.get(1).getTraineeName(), result.get(1).getTraineeName());

        verify(authManager).getCurrentUser();
        verify(trainerRepository).findByUserUsername(request.getTrainerUsername());
        verify(trainingRepository).findTrainerTrainingsByCriteria(
                request.getTrainerUsername(),
                request.getFromDate(),
                request.getToDate(),
                request.getTraineeName()
                );
    }

    @Test
    void shouldThrowExceptionWhenGetTrainerTrainingsListUnauthorizedUser() {
        // Given
        TrainerTrainingListRequest request = new TrainerTrainingListRequest(
            testTrainer.getUser().getUsername(), // Request for a different trainer
            null, null, null
        );

        // Current user is NOT the requested trainer
        User otherUser = new User(5L, "Other", "User", "other.user", "pass", true, null, null);
        when(authManager.getCurrentUser()).thenReturn(otherUser);

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.getTrainerTrainingsList(request));

        // Then
        assertEquals("User authentication required : You are not authorized to view trainings for other trainers.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verifyNoInteractions(trainerRepository);
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldThrowExceptionWhenGetTrainerTrainingsListTrainerNotFound() {
        // Given
        TrainerTrainingListRequest request = new TrainerTrainingListRequest(
            "nonexistent.trainer", // Request for non-existent trainer
            null, null, null
        );

        // Current user is the one specified in the request
        User nonexistentTrainerUser = new User(7L, "NonEx", "Trainer", "nonexistent.trainer", "pass", true, null, null);
        when(authManager.getCurrentUser()).thenReturn(nonexistentTrainerUser);


        when(trainerRepository.findByUserUsername(request.getTrainerUsername()))
            .thenReturn(Optional.empty()); // Trainer not found

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.getTrainerTrainingsList(request));

        // Then
        assertEquals("Resource not found. : Trainer with username nonexistent.trainer not found.", exception.getMessage());

        verify(authManager).getCurrentUser();
        verify(trainerRepository).findByUserUsername(request.getTrainerUsername());
        verifyNoInteractions(trainingRepository);
    }

    // --- Other methods (getTrainingById, getAllTrainings, updateTraining, deleteTrainingById) ---

    @Test
    void shouldGetTrainingByIdSuccessfullyForAssociatedTrainee() {
        // Given
        Long trainingId = 1L;
        Training training = new Training(trainingId, "Yoga", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        // Ensure current user is the associated trainee
        when(authManager.getCurrentUser()).thenReturn(testTrainee.getUser());
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(training));

        // When
        TrainingResponse response = trainingService.getTrainingById(trainingId);

        // Then
        assertNotNull(response);
        assertEquals(training.getTrainingName(), response.getTrainingName());
        // assertEquals(trainingId, response.getId()); // TrainingResponse'da ID olmadığı için kaldırıldı
        assertEquals(testTrainee.getUser().getUsername(), response.getTraineeUsername());
        assertEquals(testTrainer.getUser().getUsername(), response.getTrainerUsername());

        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
    }

    @Test
    void shouldGetTrainingByIdSuccessfullyForAssociatedTrainer() {
        // Given
        Long trainingId = 1L;
        Training training = new Training(trainingId, "Yoga", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        // Ensure current user is the associated trainer
        when(authManager.getCurrentUser()).thenReturn(testTrainer.getUser());
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(training));

        // When
        TrainingResponse response = trainingService.getTrainingById(trainingId);

        // Then
        assertNotNull(response);
        assertEquals(training.getTrainingName(), response.getTrainingName());
        // assertEquals(trainingId, response.getId()); // TrainingResponse'da ID olmadığı için kaldırıldı
        assertEquals(testTrainee.getUser().getUsername(), response.getTraineeUsername());
        assertEquals(testTrainer.getUser().getUsername(), response.getTrainerUsername());

        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
    }

    @Test
    void shouldThrowExceptionWhenGetTrainingByIdWithNullOrInvalidId() {
        // Given
        Long nullId = null;
        Long negativeId = -5L;

        // Test with null ID
        BaseException exceptionNull = assertThrows(BaseException.class, () -> trainingService.getTrainingById(nullId));
        assertEquals("Invalid parameter provided. : Training ID for lookup must be a positive value. Provided ID: null", exceptionNull.getMessage());
        // Test with negative ID
        BaseException exceptionNegative = assertThrows(BaseException.class, () -> trainingService.getTrainingById(negativeId));
        assertEquals("Invalid parameter provided. : Training ID for lookup must be a positive value. Provided ID: -5", exceptionNegative.getMessage());

        verify(authManager, times(2)).getCurrentUser(); // Called twice for two separate assertions
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldThrowExceptionWhenGetTrainingByIdNotFound() {
        // Given
        Long nonExistentId = 99L;
        when(trainingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.getTrainingById(nonExistentId));

        // Then
        assertEquals("Resource not found. : Training not found with ID: 99", exception.getMessage());

        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(nonExistentId);
    }

    @Test
    void shouldThrowExceptionWhenGetTrainingByIdUnauthorized() {
        // Given
        Long trainingId = 1L;
        
        Training training = new Training(trainingId, "Yoga", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        // Current user is neither the trainee nor the trainer
        User unauthorizedUser = new User(10L, "Unauthorized", "User", "unauth.user", "pass", true, null, null);
        when(authManager.getCurrentUser()).thenReturn(unauthorizedUser);
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(training));

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.getTrainingById(trainingId));

        // Then
        assertEquals("User authentication required : You are not authorized to view this training.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
    }

    @Test
    void shouldGetAllTrainingsSuccessfully() {
        // Given
        Training training1 = new Training(1L, "Yoga", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);
        Training training2 = new Training(2L, "Pilates", LocalDate.of(2023, 2, 1), 45, testTrainee, testTrainer, testTrainingType);
        List<Training> trainings = Arrays.asList(training1, training2);

        when(trainingRepository.findAll()).thenReturn(trainings);

        // When
        List<TrainingResponse> result = trainingService.getAllTrainings();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertEquals(training1.getTrainingName(), result.get(0).getTrainingName());
        assertEquals(training2.getTrainingName(), result.get(1).getTrainingName());

        verify(authManager).getCurrentUser();
        verify(trainingRepository).findAll();
    }

    @Test
    void shouldUpdateTrainingSuccessfully() {
        // Given
        Long trainingId = 1L;
        TrainingUpdateRequest request = new TrainingUpdateRequest(
            trainingId,
            "Updated Yoga",
            LocalDate.of(2023, 10, 27),
            75,
            testTrainer.getUser().getUsername(), // Keep same trainer
            testTrainee.getUser().getUsername(), // Keep same trainee
            testTrainingType.getTrainingTypeName() // Keep same training type
        );

        // Original training to be updated
        Training existingTraining = new Training(trainingId, "Old Yoga", LocalDate.of(2023, 10, 26), 60, testTrainee, testTrainer, testTrainingType);

        // Ensure current user is an associated party (e.g., the trainee)
        when(authManager.getCurrentUser()).thenReturn(testTrainee.getUser());
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(existingTraining));

        // Mocking for potential re-fetches if logic permits
        when(trainerRepository.findByUserUsername(testTrainer.getUser().getUsername())).thenReturn(Optional.of(testTrainer));
        when(traineeRepository.findByUserUsername(testTrainee.getUser().getUsername())).thenReturn(Optional.of(testTrainee));
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(testTrainingType.getTrainingTypeName())).thenReturn(Optional.of(testTrainingType));


        // Prepare the updated training object that save() would return
        Training updatedTraining = new Training(trainingId, request.getTrainingName(), request.getTrainingDate(),
                                                request.getTrainingDuration(), testTrainee, testTrainer, testTrainingType);
        when(trainingRepository.save(any(Training.class))).thenReturn(updatedTraining);

        // When
        TrainingResponse response = trainingService.updateTraining(request);

        // Then
        assertNotNull(response);
        // assertEquals(trainingId, response.getId()); // TrainingResponse'da ID olmadığı için kaldırıldı
        assertEquals(request.getTrainingName(), response.getTrainingName());
        assertEquals(request.getTrainingDate(), response.getTrainingDate());
        assertEquals(request.getTrainingDuration(), response.getTrainingDuration());
        assertEquals(testTrainer.getUser().getUsername(), response.getTrainerUsername());
        assertEquals(testTrainee.getUser().getUsername(), response.getTraineeUsername());
        assertEquals(testTrainingType.getTrainingTypeName(), response.getTrainingTypeName());

        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
        verify(trainerRepository).findByUserUsername(testTrainer.getUser().getUsername()); // Verify re-fetch
        verify(traineeRepository).findByUserUsername(testTrainee.getUser().getUsername()); // Verify re-fetch
        verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(testTrainingType.getTrainingTypeName()); // Verify re-fetch
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void shouldUpdateTrainingWithNewTrainerTraineeAndTypeSuccessfully() {
        // Given
        Long trainingId = 1L;

        // New Trainer
        User newTrainerUser = new User(100L, "New", "Trainer", "new.trainer", "pass", true, null, null);
        TrainingType newTrainingType = new TrainingType(200L, "Weightlifting");
        
        Trainer newTrainer = new Trainer(101L, newTrainingType, newTrainerUser, new HashSet<>(), new HashSet<>());

        // New Trainee
        User newTraineeUser = new User(102L, "New", "Trainee", "new.trainee", "pass", true, null, null);
        
        Trainee newTrainee = new Trainee(103L, LocalDate.of(1995, 5, 5), "New Address", newTraineeUser, new HashSet<>(), new HashSet<>());

        
        TrainingType brandNewTrainingType = new TrainingType(300L, "Cardio");

        TrainingUpdateRequest request = new TrainingUpdateRequest(
            trainingId,
            "Major Update",
            LocalDate.of(2024, 1, 1),
            90,
            newTrainer.getUser().getUsername(),
            newTrainee.getUser().getUsername(),
            brandNewTrainingType.getTrainingTypeName()
        );

        // Original training to be updated
        Training existingTraining = new Training(trainingId, "Old Training", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        // Ensure current user is an associated party (e.g., the trainee from existing training)
        when(authManager.getCurrentUser()).thenReturn(testTrainee.getUser());
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(existingTraining));

        // Mocking for fetching new trainer, trainee, training type
        when(trainerRepository.findByUserUsername(newTrainer.getUser().getUsername())).thenReturn(Optional.of(newTrainer));
        when(traineeRepository.findByUserUsername(newTrainee.getUser().getUsername())).thenReturn(Optional.of(newTrainee));
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(brandNewTrainingType.getTrainingTypeName())).thenReturn(Optional.of(brandNewTrainingType));

        // Prepare the updated training object that save() would return
        Training savedUpdatedTraining = new Training(trainingId, request.getTrainingName(), request.getTrainingDate(),
                                                     request.getTrainingDuration(), newTrainee, newTrainer, brandNewTrainingType);
        when(trainingRepository.save(any(Training.class))).thenReturn(savedUpdatedTraining);

        // When
        TrainingResponse response = trainingService.updateTraining(request);

        // Then
        assertNotNull(response);
        // assertEquals(trainingId, response.getId()); // TrainingResponse'da ID olmadığı için kaldırıldı
        assertEquals(request.getTrainingName(), response.getTrainingName());
        assertEquals(request.getTrainingDate(), response.getTrainingDate());
        assertEquals(request.getTrainingDuration(), response.getTrainingDuration());
        assertEquals(newTrainer.getUser().getUsername(), response.getTrainerUsername());
        assertEquals(newTrainee.getUser().getUsername(), response.getTraineeUsername());
        assertEquals(brandNewTrainingType.getTrainingTypeName(), response.getTrainingTypeName());

        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
        verify(trainerRepository).findByUserUsername(newTrainer.getUser().getUsername());
        verify(traineeRepository).findByUserUsername(newTrainee.getUser().getUsername());
        verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(brandNewTrainingType.getTrainingTypeName());
        verify(trainingRepository).save(any(Training.class));
    }


    @Test
    void shouldThrowExceptionWhenUpdateTrainingWithNullOrInvalidId() {
        // Given
        TrainingUpdateRequest nullIdRequest = new TrainingUpdateRequest(null, "Name", null, null, null, null, null);
        TrainingUpdateRequest negativeIdRequest = new TrainingUpdateRequest(-5L, "Name", null, null, null, null, null);

        // Test with null ID
        BaseException exceptionNull = assertThrows(BaseException.class, () -> trainingService.updateTraining(nullIdRequest));
        assertEquals("Invalid parameter provided. : Training ID for update must be a positive value. Provided ID: null", exceptionNull.getMessage());
        // Test with negative ID
        BaseException exceptionNegative = assertThrows(BaseException.class, () -> trainingService.updateTraining(negativeIdRequest));
        assertEquals("Invalid parameter provided. : Training ID for update must be a positive value. Provided ID: -5", exceptionNegative.getMessage());

        verify(authManager, times(2)).getCurrentUser();
        verifyNoInteractions(trainingRepository);
        verifyNoInteractions(trainerRepository);
        verifyNoInteractions(traineeRepository);
        verifyNoInteractions(trainingTypeRepository);
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainingNotFound() {
        // Given
        Long nonExistentId = 99L;
        TrainingUpdateRequest request = new TrainingUpdateRequest(nonExistentId, "Name", null, null, null, null, null);

        when(trainingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.updateTraining(request));

        // Then
        assertEquals("Resource not found. : Training with ID 99 not found.", exception.getMessage());

        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(nonExistentId);
        verifyNoInteractions(trainerRepository);
        verifyNoInteractions(traineeRepository);
        verifyNoInteractions(trainingTypeRepository);
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainingUnauthorized() {
        // Given
        Long trainingId = 1L;
        TrainingUpdateRequest request = new TrainingUpdateRequest(trainingId, "Updated Name", null, null, null, null, null);

        // Training associated with testTrainee and testTrainer
        Training existingTraining = new Training(trainingId, "Old Training", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        // Current user is neither the trainee nor the trainer
        User unauthorizedUser = new User(10L, "Unauthorized", "User", "unauth.user", "pass", true, null, null);
        when(authManager.getCurrentUser()).thenReturn(unauthorizedUser);
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(existingTraining));

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.updateTraining(request));

        // Then
        assertEquals("User authentication required : You are not authorized to update this training.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
        verifyNoInteractions(trainerRepository);
        verifyNoInteractions(traineeRepository);
        verifyNoInteractions(trainingTypeRepository);
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainingNewTrainerNotFound() {
        // Given
        Long trainingId = 1L;
        String nonExistentTrainerUsername = "nonexistent.trainer";
        TrainingUpdateRequest request = new TrainingUpdateRequest(trainingId, "Name", null, null, nonExistentTrainerUsername, null, null);

        Training existingTraining = new Training(trainingId, "Old Training", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        // testTraineeUser'ın yetkili kullanıcı olduğunu belirtiyoruz
        when(authManager.getCurrentUser()).thenReturn(testTrainee.getUser());
        // trainingRepository'nin mevcut eğitimi döndürmesini sağlıyoruz
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(existingTraining));
        // trainerRepository'nin olmayan eğitmen için boş Optional döndürmesini sağlıyoruz
        when(trainerRepository.findByUserUsername(nonExistentTrainerUsername)).thenReturn(Optional.empty());

        // When
        // trainingService.updateTraining metodunu çağırdığımızda BaseException fırlatılmasını bekliyoruz
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.updateTraining(request));

        // Then
        assertEquals("Resource not found. : New Trainer with username 'nonexistent.trainer' not found.", exception.getMessage());
        
        // Mock'ların doğru çağrıldığını doğruluyoruz
        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId); 
        verify(trainerRepository).findByUserUsername(nonExistentTrainerUsername);

        // Yeni eğitmen bulunamadığı için güncelleme başarılı olmamalı ve kaydedilmemeli
        verify(trainingRepository, never()).save(any(Training.class)); 

        // Trainee ve TrainingType repository'lerinde hiçbir etkileşim olmadığını doğruluyoruz
        // çünkü request'te bu alanlar null olarak ayarlandı
        verifyNoInteractions(traineeRepository);
        verifyNoInteractions(trainingTypeRepository);
        
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainingNewTrainerNotActive() {
        // Given
        Long trainingId = 1L;
        User inactiveTrainerUser = new User(100L, "Inact", "Trainer", "inact.trainer", "pass", false, null, null);
        // Trainer constructor: id, specialization, user, trainees (HashSet), trainings (HashSet)
        Trainer inactiveTrainer = new Trainer(101L, testTrainingType, inactiveTrainerUser, new HashSet<>(), new HashSet<>());

        TrainingUpdateRequest request = new TrainingUpdateRequest(trainingId, "Name", null, null, inactiveTrainer.getUser().getUsername(), null, null);

        Training existingTraining = new Training(trainingId, "Old Training", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        when(authManager.getCurrentUser()).thenReturn(testTrainee.getUser()); // Authorized user
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(existingTraining));
        when(trainerRepository.findByUserUsername(inactiveTrainer.getUser().getUsername())).thenReturn(Optional.of(inactiveTrainer));

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.updateTraining(request));

        // Then
        assertEquals("User is not active : New Trainer 'inact.trainer' is not active. Cannot update training.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
        verify(trainerRepository).findByUserUsername(inactiveTrainer.getUser().getUsername());
        verifyNoInteractions(traineeRepository);
        verifyNoInteractions(trainingTypeRepository);
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainingNewTraineeNotFound() {
        // Given
        Long trainingId = 1L;
        String nonExistentTraineeUsername = "nonexistent.trainee";
        TrainingUpdateRequest request = new TrainingUpdateRequest(trainingId, "Name", null, null, null, nonExistentTraineeUsername, null);

        Training existingTraining = new Training(trainingId, "Old Training", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        when(authManager.getCurrentUser()).thenReturn(testTrainer.getUser()); // Authorized user
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(existingTraining));
        when(traineeRepository.findByUserUsername(nonExistentTraineeUsername)).thenReturn(Optional.empty());

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.updateTraining(request));

        // Then
        assertEquals("Resource not found. : New Trainee with username 'nonexistent.trainee' not found.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
        verify(traineeRepository).findByUserUsername(nonExistentTraineeUsername);
        verifyNoInteractions(trainingTypeRepository);
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainingNewTraineeNotActive() {
        // Given
        Long trainingId = 1L;
        // Declare and initialize inactiveTraineeUser and inactiveTrainee directly in the test method
        User inactiveTraineeUser = new User(100L, "Inact", "Trainee", "inact.trainee", "pass", false, null, null);
        Trainee inactiveTrainee = new Trainee(101L, LocalDate.of(1990, 1, 1), "Some Address", inactiveTraineeUser, new HashSet<>(), new HashSet<>());

        TrainingUpdateRequest request = new TrainingUpdateRequest(trainingId, "Name", null, null, null, inactiveTrainee.getUser().getUsername(), null);

        Training existingTraining = new Training(trainingId, "Old Training", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        // Configure mock behavior:
        when(authManager.getCurrentUser()).thenReturn(testTrainer.getUser()); // Authorized user
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(existingTraining));
        when(traineeRepository.findByUserUsername(inactiveTrainee.getUser().getUsername())).thenReturn(Optional.of(inactiveTrainee));

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.updateTraining(request));

        // Then
        // Verify the exact error message thrown by the service
        assertEquals("User is not active : New Trainee 'inact.trainee' is not active. Cannot update training.", exception.getMessage());
        
        // Verify specific interactions with mocks:
        verify(authManager).getCurrentUser(); // Auth manager was used
        verify(trainingRepository).findById(trainingId); // Existing training was looked up
        verify(traineeRepository).findByUserUsername(inactiveTrainee.getUser().getUsername()); // Inactive trainee was looked up

        // Crucially, verify that trainingRepository.save() was NEVER called,
        // as the update should fail due to the inactive trainee.
        verify(trainingRepository, never()).save(any(Training.class)); // FIX: Replaced verifyNoInteractions

        // Verify that no interactions occurred with other repositories,
        // as their respective fields in the request were null.
        verifyNoInteractions(trainerRepository);
        verifyNoInteractions(trainingTypeRepository);
    }

    @Test
    void shouldThrowExceptionWhenUpdateTrainingNewTrainingTypeNotFound() {
        // Given
        Long trainingId = 1L;
        String nonExistentTrainingTypeName = "NonExistentType";
        TrainingUpdateRequest request = new TrainingUpdateRequest(trainingId, "Name", null, null, null, null, nonExistentTrainingTypeName);

        Training existingTraining = new Training(trainingId, "Old Training", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        when(authManager.getCurrentUser()).thenReturn(testTrainee.getUser()); // Authorized user
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(existingTraining));
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(nonExistentTrainingTypeName)).thenReturn(Optional.empty());

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.updateTraining(request));

        // Then
        assertEquals("Resource not found. : New Training Type 'NonExistentType' not found.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
        verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(nonExistentTrainingTypeName);
        verify(trainingRepository, never()).save(any(Training.class));
        }

    @Test
    void shouldDeleteTrainingByIdSuccessfully() {
        // Given
        Long trainingId = 1L;
        Training trainingToDelete = new Training(trainingId, "Yoga", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        // Assume current user is the associated trainee
        when(authManager.getCurrentUser()).thenReturn(testTrainee.getUser());
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(trainingToDelete));
        doNothing().when(trainingRepository).delete(trainingToDelete);

        // When
        trainingService.deleteTrainingById(trainingId);

        // Then
        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
        verify(trainingRepository).delete(trainingToDelete);
    }

    @Test
    void shouldThrowExceptionWhenDeleteTrainingByIdWithNullOrInvalidId() {
        // Given
        Long nullId = null;
        Long negativeId = -5L;

        // Test with null ID
        BaseException exceptionNull = assertThrows(BaseException.class, () -> trainingService.deleteTrainingById(nullId));
        assertEquals("Invalid parameter provided. : Training ID for deletion must be a positive value. Provided ID: null", exceptionNull.getMessage());

        // Test with negative ID
        BaseException exceptionNegative = assertThrows(BaseException.class, () -> trainingService.deleteTrainingById(negativeId));
        assertEquals("Invalid parameter provided. : Training ID for deletion must be a positive value. Provided ID: -5", exceptionNegative.getMessage());

        verify(authManager, times(2)).getCurrentUser();
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void shouldThrowExceptionWhenDeleteTrainingByIdNotFound() {
        // Given
        Long nonExistentId = 99L;
        when(trainingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.deleteTrainingById(nonExistentId));

        // Then
        assertEquals("Resource not found. : Training with ID 99 not found. Cannot delete.", exception.getMessage());

        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(nonExistentId);
        verifyNoMoreInteractions(trainingRepository); // No delete should be called
    }

    @Test
    void shouldThrowExceptionWhenDeleteTrainingByIdUnauthorized() {
        // Given
        Long trainingId = 1L;
        Training trainingToDelete = new Training(trainingId, "Yoga", LocalDate.of(2023, 1, 1), 60, testTrainee, testTrainer, testTrainingType);

        // Current user is neither the trainee nor the trainer
        User unauthorizedUser = new User(10L, "Unauthorized", "User", "unauth.user", "pass", true, null, null);
        when(authManager.getCurrentUser()).thenReturn(unauthorizedUser);
        when(trainingRepository.findById(trainingId)).thenReturn(Optional.of(trainingToDelete));

        // When
        BaseException exception = assertThrows(BaseException.class, () -> trainingService.deleteTrainingById(trainingId));

        // Then
        assertEquals("User authentication required : You are not authorized to delete this training.", exception.getMessage());
        verify(authManager).getCurrentUser();
        verify(trainingRepository).findById(trainingId);
        verifyNoMoreInteractions(trainingRepository); // No delete should be called
    }
}