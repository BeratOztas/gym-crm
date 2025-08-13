package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym_crm.api.dto.request.UserActivationRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerCreateRequest;
import com.epam.gym_crm.api.dto.request.trainer.TrainerUpdateRequest;
import com.epam.gym_crm.api.dto.response.TrainerInfoResponse;
import com.epam.gym_crm.api.dto.response.TrainerProfileResponse;
import com.epam.gym_crm.api.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.auth.AuthManager;
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
	private AuthManager authManager;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private TrainerServiceImpl trainerService;

	@Mock
	private AppMetrics appMetrics;

	private User testUser;
	private Trainer testTrainer;
	private TrainingType testSpecialization;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);
		testUser.setFirstName("John");
		testUser.setLastName("Doe");
		testUser.setUsername("John.Doe");
		testUser.setPassword("password");
		testUser.setActive(true);

		testSpecialization = new TrainingType();
		testSpecialization.setId(1L);
		testSpecialization.setTrainingTypeName("Fitness");

		testTrainer = new Trainer();
		testTrainer.setId(1L);
		testTrainer.setUser(testUser);
		testTrainer.setSpecialization(testSpecialization);
		testTrainer.setTrainings(new HashSet<>());
		
		trainerService = new TrainerServiceImpl(
		        authenticationService,
		        trainerRepository,
		        trainingTypeRepository,
		        authManager,
		        userRepository,
		        trainingRepository,
		        traineeRepository,
		        appMetrics
		    );
	}

	// --- 1. findTrainerById(Long id) ---
	@Test
	void shouldFindTrainerByIdSuccessfully() {
		// Given
		Long trainerId = 1L;

		when(authManager.getCurrentUser()).thenReturn(testUser);

		when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(testTrainer));

		// When
		TrainerProfileResponse response = trainerService.findTrainerById(trainerId);

		// Then
		assertNotNull(response);
		assertEquals(testTrainer.getUser().getUsername(), response.getUsername());
		verify(authManager).getCurrentUser();
		verify(trainerRepository).findById(trainerId);
	}

	@Test
	void shouldThrowExceptionWhenFindTrainerByIdWithNullOrInvalidId() {
		// Given
		Long invalidId = 0L;

		when(authManager.getCurrentUser()).thenReturn(testUser);

		// When
		BaseException exception = assertThrows(BaseException.class, () -> trainerService.findTrainerById(invalidId));

		// Then
		assertEquals("Invalid parameter provided. : Trainer ID must be a positive value. Provided: " + invalidId,
				exception.getMessage());
		verify(authManager).getCurrentUser();
		verifyNoInteractions(trainerRepository);
	}

	@Test
	void shouldThrowExceptionWhenFindTrainerByIdNotFound() {
		// Given
		Long nonExistentId = 99L;

		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(trainerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

		// When
		BaseException exception = assertThrows(BaseException.class,
				() -> trainerService.findTrainerById(nonExistentId));

		// Then
		assertEquals("Resource not found. : Trainer not found with ID: " + nonExistentId, exception.getMessage());
		verify(authManager).getCurrentUser();
		verify(trainerRepository).findById(nonExistentId);
	}

	// --- 2. findTrainerByUsername(String username) ---
	@Test
	void shouldFindTrainerByUsernameSuccessfully() {
		// Given
		String username = "John.Doe";

		// authManager.getCurrentUser() çağrısını stub'la
		when(authManager.getCurrentUser()).thenReturn(testUser);

		when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(testTrainer));

		// When
		TrainerProfileResponse response = trainerService.findTrainerByUsername(username);

		// Then
		assertNotNull(response);
		assertEquals(username, response.getUsername());
		verify(authManager).getCurrentUser();
		verify(trainerRepository).findByUserUsername(username);
	}

	@Test
	void shouldThrowExceptionWhenFindTrainerByUsernameWithNullOrEmpty() {
		// Given
		String nullUsername = null;
		String emptyUsername = "";

		when(authManager.getCurrentUser()).thenReturn(testUser);

		// NullPointerException yerine BaseException fırlatılmasını bekliyoruz
		BaseException exceptionNull = assertThrows(BaseException.class,
				() -> trainerService.findTrainerByUsername(nullUsername));
		assertEquals("Invalid parameter provided. : Trainer username must not be null or empty.",
				exceptionNull.getMessage());

		verify(authManager).getCurrentUser();

		verifyNoInteractions(trainerRepository);

		// Tekrar NullPointerException yerine BaseException fırlatılmasını bekliyoruz.
		BaseException exceptionEmpty = assertThrows(BaseException.class,
				() -> trainerService.findTrainerByUsername(emptyUsername));
		assertEquals("Invalid parameter provided. : Trainer username must not be null or empty.",
				exceptionEmpty.getMessage());

		verify(authManager, times(2)).getCurrentUser();

		verifyNoInteractions(trainerRepository);
	}

	@Test
	void shouldThrowExceptionWhenFindTrainerByUsernameNotFound() {
		// Given
		String nonExistentUsername = "NonExistent.Trainer";
		// authManager.getCurrentUser() çağrısını stub'la
		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(trainerRepository.findByUserUsername(nonExistentUsername)).thenReturn(Optional.empty());

		// When
		BaseException exception = assertThrows(BaseException.class,
				() -> trainerService.findTrainerByUsername(nonExistentUsername));

		// Then
		assertEquals("Resource not found. : Trainer not found with username: " + nonExistentUsername,
				exception.getMessage());
		verify(authManager).getCurrentUser();
		verify(trainerRepository).findByUserUsername(nonExistentUsername);
	}

	// --- 3. getAllTrainers() ---

	@Test
	void shouldGetAllTrainersSuccessfully() {
		// Given
		when(authManager.getCurrentUser()).thenReturn(testUser);

		Trainer trainer2 = new Trainer();
		trainer2.setId(2L);
		User user2 = new User();
		user2.setUsername("Jane.Doe");
		trainer2.setUser(user2);
		trainer2.setSpecialization(testSpecialization);

		when(trainerRepository.findAll()).thenReturn(Arrays.asList(testTrainer, trainer2));

		// When
		List<TrainerProfileResponse> responseList = trainerService.getAllTrainers();

		// Then
		assertNotNull(responseList);
		assertEquals(2, responseList.size());
		assertEquals("John.Doe", responseList.get(0).getUsername());
		assertEquals("Jane.Doe", responseList.get(1).getUsername());
		verify(authManager).getCurrentUser();
		verify(trainerRepository).findAll();
	}

	@Test
	void shouldReturnEmptyListWhenNoTrainersExist() {
		// Given
		when(authManager.getCurrentUser()).thenReturn(testUser);

		when(trainerRepository.findAll()).thenReturn(List.of());

		// When
		List<TrainerProfileResponse> responseList = trainerService.getAllTrainers();

		// Then
		assertNotNull(responseList);
		assertTrue(responseList.isEmpty());
		verify(authManager).getCurrentUser();
		verify(trainerRepository).findAll();
	}

//  --- 4. getUnassignedTrainersForTrainee(String traineeUsername) ---
	@Test
	void shouldGetUnassignedTrainersForTraineeSuccessfully() {
		// Given
		String traineeUsername = "Test.Trainee";
		User traineeUser = new User(2L, "Test", "Trainee", traineeUsername, "pass", true, null, null);
		Trainee trainee = new Trainee(2L, null, null, traineeUser, new HashSet<>(), new HashSet<>());

		// Aktif olan tüm antrenörler
		Trainer assignedTrainer = new Trainer();
		assignedTrainer.setId(1L);
		User assignedUser = new User(1L, "Assigned", "One", "Assigned.One", "pass", true, null, null);
		assignedTrainer.setUser(assignedUser);
		assignedTrainer.setSpecialization(testSpecialization);
		assignedTrainer.setTrainings(new HashSet<>());

		Trainer unassignedTrainer1 = new Trainer();
		unassignedTrainer1.setId(3L);
		User unassignedUser1 = new User(3L, "Unassigned", "One", "Unassigned.One", "pass", true, null, null);
		unassignedTrainer1.setUser(unassignedUser1);
		unassignedTrainer1.setSpecialization(testSpecialization);
		unassignedTrainer1.setTrainings(new HashSet<>());

		Trainer unassignedTrainer2 = new Trainer();
		unassignedTrainer2.setId(4L);
		User unassignedUser2 = new User(4L, "Unassigned", "Two", "Unassigned.Two", "pass", true, null, null);
		unassignedTrainer2.setUser(unassignedUser2);
		unassignedTrainer2.setSpecialization(testSpecialization);
		unassignedTrainer2.setTrainings(new HashSet<>());

		List<Trainer> allActiveTrainers = Arrays.asList(assignedTrainer, unassignedTrainer1, unassignedTrainer2);

		// Trainee'ye atanmış antrenörler (sadece 'assignedTrainer' atanmış)
		trainee.setTrainers(new HashSet<>(Arrays.asList(assignedTrainer)));

		when(authManager.getCurrentUser()).thenReturn(traineeUser);
		when(traineeRepository.findByUserUsername(traineeUsername)).thenReturn(Optional.of(trainee));

		// Yeni metot çağrısı mock'lanıyor
		when(trainerRepository.findByUserIsActive(true)).thenReturn(allActiveTrainers);

		// When
		List<TrainerInfoResponse> result = trainerService.getUnassignedTrainersForTrainee(traineeUsername);

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());

		// Atanmamış antrenörlerin listesinde atanmış olanın olmadığından emin ol
		List<String> resultUsernames = result.stream().map(TrainerInfoResponse::getUsername)
				.collect(Collectors.toList());

		assertTrue(resultUsernames.contains("Unassigned.One"));
		assertTrue(resultUsernames.contains("Unassigned.Two"));
		assertFalse(resultUsernames.contains("Assigned.One"));

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(traineeUsername);

		// Yeni metot çağrısının yapıldığını doğrula
		verify(trainerRepository).findByUserIsActive(true);

		// Artık var olmayan metot çağrısını doğrulama
		// verify(trainerRepository,
		// never()).findActiveTrainersNotAssignedToTrainee(anyString());
	}

	@Test
	void shouldThrowExceptionWhenGetUnassignedTrainersForInactiveTrainee() {
		// Given
		String inactiveTraineeUsername = "Inactive.Trainee";
		User inactiveTraineeUser = new User(2L, "Inactive", "Trainee", inactiveTraineeUsername, "pass", false, null,
				null);
		Trainee inactiveTrainee = new Trainee(2L, null, null, inactiveTraineeUser, new HashSet<>(), new HashSet<>());

		// authManager.getCurrentUser() çağrısını stub'la
		when(authManager.getCurrentUser()).thenReturn(inactiveTraineeUser);
		when(traineeRepository.findByUserUsername(inactiveTraineeUsername)).thenReturn(Optional.of(inactiveTrainee));

		// When
		BaseException exception = assertThrows(BaseException.class,
				() -> trainerService.getUnassignedTrainersForTrainee(inactiveTraineeUsername));

		assertEquals("User is not active : Trainee " + inactiveTraineeUsername
				+ " is not active. Cannot retrieve unassigned trainers.", exception.getMessage());

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(inactiveTraineeUsername);
		verifyNoInteractions(trainerRepository);
	}

	// --- 5. createTrainer(TrainerCreateRequest request) ---
	@Test
	void shouldCreateTrainerSuccessfully() {
		// Given
		TrainerCreateRequest request = new TrainerCreateRequest("Jane", "Doe", "Fitness");
		User createdUser = new User(2L, "Jane", "Doe", "Jane.Doe", "generatedPass", true, null, null);

		when(authenticationService.createAndSaveUser(request.getFirstName(), request.getLastName()))
				.thenReturn(createdUser);
		when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(request.getSpecialization()))
				.thenReturn(Optional.of(testSpecialization));

		Trainer expectedSavedTrainer = new Trainer();
		expectedSavedTrainer.setId(2L);
		expectedSavedTrainer.setUser(createdUser);
		expectedSavedTrainer.setSpecialization(testSpecialization);
		expectedSavedTrainer.setTrainings(new HashSet<>());

		when(trainerRepository.save(any(Trainer.class))).thenReturn(expectedSavedTrainer);

		UserRegistrationResponse response = trainerService.createTrainer(request);

		// Then
		assertNotNull(response);
		// Dönüş nesnesinin sadece username ve password içerdiğini kontrol et
		assertEquals(createdUser.getUsername(), response.getUsername());
		assertEquals(createdUser.getPassword(), response.getPassword());

		// Metotların doğru çağrıldığını doğruluyoruz
		verify(authenticationService).createAndSaveUser(request.getFirstName(), request.getLastName());
		verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(request.getSpecialization());
		verify(trainerRepository).save(any(Trainer.class));
	}

	@Test
	void shouldThrowExceptionWhenCreateTrainerRequestIsNull() {
		// Given
		TrainerCreateRequest nullRequest = null;

		// When
		BaseException exception = assertThrows(BaseException.class, () -> trainerService.createTrainer(nullRequest));

		// Then
		assertEquals("Invalid parameter provided. : Trainer must not be null", exception.getMessage());
		verifyNoInteractions(authenticationService);
		verifyNoInteractions(trainingTypeRepository);
		verifyNoInteractions(trainerRepository);
	}

	@Test
	void shouldThrowExceptionWhenCreateTrainerTrainingTypeNotFound() {
		// Given
		TrainerCreateRequest request = new TrainerCreateRequest("Jane", "Doe", "NonExistentType");
		User createdUser = new User(2L, "Jane", "Doe", "Jane.Doe", "generatedPass", true, null, null);

		when(authenticationService.createAndSaveUser(anyString(), anyString())).thenReturn(createdUser);
		when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(request.getSpecialization()))
				.thenReturn(Optional.empty());

		// When
		BaseException exception = assertThrows(BaseException.class, () -> trainerService.createTrainer(request));

		// Then
		assertEquals("Entity Not Found. : Training type not found: " + request.getSpecialization(),
				exception.getMessage());
		verify(authenticationService).createAndSaveUser(request.getFirstName(), request.getLastName());
		verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(request.getSpecialization());
		verifyNoInteractions(trainerRepository);
	}

	@Test
	void shouldThrowExceptionWhenCreateTrainerFailedToSave() {
		// Given
		TrainerCreateRequest request = new TrainerCreateRequest("Jane", "Doe", "Fitness");
		User createdUser = new User(2L, "Jane", "Doe", "Jane.Doe", "generatedPass", true, null, null);

		when(authenticationService.createAndSaveUser(anyString(), anyString())).thenReturn(createdUser);
		when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(request.getSpecialization()))
				.thenReturn(Optional.of(testSpecialization));
		when(trainerRepository.save(any(Trainer.class))).thenReturn(null); // Simulate save failure

		// When
		BaseException exception = assertThrows(BaseException.class, () -> trainerService.createTrainer(request));

		// Then
		assertEquals("An unexpected error occurred. : Failed to create trainer profile.", exception.getMessage());
		verify(authenticationService).createAndSaveUser(request.getFirstName(), request.getLastName());
		verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(request.getSpecialization());
		verify(trainerRepository).save(any(Trainer.class));
	}

	// --- 6. updateTrainer(TrainerUpdateRequest request) ---
	@Test
	void shouldUpdateTrainerSuccessfully() {
		// Given
		TrainerUpdateRequest request = new TrainerUpdateRequest("John.Doe", "Johnny", "Dorian", "Yoga", true);
		Trainer existingTrainer = new Trainer();
		existingTrainer.setId(1L);
		existingTrainer.setUser(new User(testUser.getId(), "John", "Doe", "John.Doe", "password", true, null, null));
		existingTrainer.setSpecialization(testSpecialization); // "Fitness"

		TrainingType newSpecialization = new TrainingType(2L, "Yoga");

		// authManager.getCurrentUser() çağrısını stub'la
		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(trainerRepository.findByUserUsername(request.getUsername())).thenReturn(Optional.of(existingTrainer));
		when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(request.getSpecialization()))
				.thenReturn(Optional.of(newSpecialization));
		when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);

		// When
		TrainerProfileResponse response = trainerService.updateTrainer(request);

		// Then
		assertNotNull(response);
		assertEquals(request.getFirstName(), response.getFirstName());
		assertEquals(request.getLastName(), response.getLastName());
		assertEquals(request.getSpecialization(), response.getSpecialization());
		verify(authManager).getCurrentUser();
		verify(trainerRepository).findByUserUsername(request.getUsername());
		verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(request.getSpecialization());
		verify(trainerRepository).save(existingTrainer);
	}

	@Test
	void shouldThrowExceptionWhenUpdateTrainerRequestIsNull() {
		// Given
		TrainerUpdateRequest nullRequest = null;
		// authManager.getCurrentUser() çağrısını stub'la
		when(authManager.getCurrentUser()).thenReturn(testUser);

		// When
		BaseException exception = assertThrows(BaseException.class, () -> trainerService.updateTrainer(nullRequest));

		// Then
		assertEquals("Invalid parameter provided. : Update request or username must not be null/empty.",
				exception.getMessage());
		verify(authManager).getCurrentUser();
		verifyNoInteractions(trainerRepository);
		verifyNoInteractions(trainingTypeRepository);
	}

	@Test
	void shouldThrowExceptionWhenUpdateTrainerUnauthorized() {
		// Given
		TrainerUpdateRequest request = new TrainerUpdateRequest("Other.Trainer", "New", "Name", "Yoga", true);
		User currentUser = new User(1L, "Current", "User", "Current.User", "pass", true, null, null);

		// authManager.getCurrentUser() çağrısını stub'la
		when(authManager.getCurrentUser()).thenReturn(currentUser);

		// When
		BaseException exception = assertThrows(BaseException.class, () -> trainerService.updateTrainer(request));

		// Then
		assertEquals("You are not authorized. : You are not authorized to update this Trainer profile.",
				exception.getMessage());
		verify(authManager).getCurrentUser();
		verifyNoInteractions(trainerRepository);
		verifyNoInteractions(trainingTypeRepository);
	}

	@Test
	void shouldThrowExceptionWhenUpdateTrainerNotFound() {
		// Given
		TrainerUpdateRequest request = new TrainerUpdateRequest("NonExistent.Trainer", "New", "Name", "Yoga", true);
		User authorizedUser = new User(5L, "Auth", "User", request.getUsername(), "pass", true, null, null);
		// authManager.getCurrentUser() çağrısını stub'la
		when(authManager.getCurrentUser()).thenReturn(authorizedUser);

		when(trainerRepository.findByUserUsername(request.getUsername())).thenReturn(Optional.empty());

		// When
		BaseException exception = assertThrows(BaseException.class, () -> trainerService.updateTrainer(request));

		// Then
		assertEquals("Resource not found. : Trainer profile not found.", exception.getMessage());
		verify(authManager).getCurrentUser();
		verify(trainerRepository).findByUserUsername(request.getUsername());
		verifyNoInteractions(trainingTypeRepository);
		verify(trainerRepository, never()).save(any(Trainer.class));
	}

	@Test
	void shouldThrowExceptionWhenUpdateTrainerTrainingTypeNotFound() {
		// Given
		TrainerUpdateRequest request = new TrainerUpdateRequest("John.Doe", "Johnny", "Dorian", "NonExistentType",
				true);
		Trainer existingTrainer = new Trainer();
		existingTrainer.setId(1L);
		existingTrainer.setUser(new User(testUser.getId(), "John", "Doe", "John.Doe", "password", true, null, null));
		existingTrainer.setSpecialization(testSpecialization);

		// authManager.getCurrentUser() çağrısını stub'la
		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(trainerRepository.findByUserUsername(request.getUsername())).thenReturn(Optional.of(existingTrainer));
		when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(request.getSpecialization()))
				.thenReturn(Optional.empty());

		// When
		BaseException exception = assertThrows(BaseException.class, () -> trainerService.updateTrainer(request));

		// Then
		assertEquals("Entity Not Found. : Training type not found: " + request.getSpecialization(),
				exception.getMessage());

		verify(authManager).getCurrentUser();
		verify(trainerRepository).findByUserUsername(request.getUsername());
		verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(request.getSpecialization());
		verify(trainerRepository, never()).save(any(Trainer.class));
	}

	// --- 7. activateDeactivateTrainer(UserActivationRequest request) ---
	@Test
	void shouldActivateTrainerSuccessfully() {
		// Given
		UserActivationRequest request = new UserActivationRequest("John.Doe", true);
		// authManager.getCurrentUser() çağrısını stub'la
		when(authManager.getCurrentUser()).thenReturn(testUser);

		Trainer existingTrainer = new Trainer();
		existingTrainer.setId(1L);
		existingTrainer.setUser(new User(testUser.getId(), testUser.getFirstName(), testUser.getLastName(),
				testUser.getUsername(), testUser.getPassword(), false, null, null));
		existingTrainer.setSpecialization(testSpecialization);
		when(trainerRepository.findByUserUsername(request.getUsername())).thenReturn(Optional.of(existingTrainer));

		when(userRepository.save(any(User.class))).thenReturn(existingTrainer.getUser());
		when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);

		// When
		trainerService.activateDeactivateTrainer(request);

		// Then
		assertTrue(existingTrainer.getUser().isActive());
		verify(authManager).getCurrentUser();
		verify(trainerRepository).findByUserUsername(request.getUsername());
		verify(userRepository).save(existingTrainer.getUser());
		verify(trainerRepository).save(existingTrainer);
	}

	@Test
	void shouldDeactivateTrainerSuccessfully() {
		// Given
		UserActivationRequest request = new UserActivationRequest("John.Doe", false);
		when(authManager.getCurrentUser()).thenReturn(testUser);

		Trainer existingTrainer = new Trainer();
		existingTrainer.setId(1L);
		existingTrainer.setUser(new User(testUser.getId(), testUser.getFirstName(), testUser.getLastName(),
				testUser.getUsername(), testUser.getPassword(), true, null, null));
		existingTrainer.setSpecialization(testSpecialization);
		when(trainerRepository.findByUserUsername(request.getUsername())).thenReturn(Optional.of(existingTrainer));

		when(userRepository.save(any(User.class))).thenReturn(existingTrainer.getUser());
		when(trainerRepository.save(any(Trainer.class))).thenReturn(existingTrainer);

		// When
		trainerService.activateDeactivateTrainer(request);

		// Then
		assertFalse(existingTrainer.getUser().isActive());
		verify(authManager).getCurrentUser();
		verify(trainerRepository).findByUserUsername(request.getUsername());
		verify(userRepository).save(existingTrainer.getUser());
		verify(trainerRepository).save(existingTrainer);
	}

	@Test
	void shouldDoNothingWhenTrainerAlreadyInDesiredState() {
		// Given
		UserActivationRequest request = new UserActivationRequest("John.Doe", true);
		// authManager.getCurrentUser() çağrısını stub'la
		when(authManager.getCurrentUser()).thenReturn(testUser);

		Trainer existingTrainer = new Trainer();
		existingTrainer.setId(1L);
		existingTrainer.setUser(new User(testUser.getId(), testUser.getFirstName(), testUser.getLastName(),
				testUser.getUsername(), testUser.getPassword(), true, null, null));
		existingTrainer.setSpecialization(testSpecialization);

		when(trainerRepository.findByUserUsername(request.getUsername())).thenReturn(Optional.of(existingTrainer));

		// When
		trainerService.activateDeactivateTrainer(request);

		// Then
		assertTrue(existingTrainer.getUser().isActive());

		verify(authManager).getCurrentUser();
		verify(trainerRepository).findByUserUsername(request.getUsername());

		verify(userRepository, never()).save(any(User.class));
		verify(trainerRepository, never()).save(any(Trainer.class));
	}

	@Test
	void shouldThrowExceptionWhenActivateDeactivateTrainerRequestIsNull() {
		// Given
		UserActivationRequest nullRequest = null;

		when(authManager.getCurrentUser()).thenReturn(testUser);

		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> trainerService.activateDeactivateTrainer(nullRequest));

		assertEquals(
				"Cannot invoke \"com.epam.gym_crm.api.dto.request.UserActivationRequest.getUsername()\" because \"request\" is null",
				exception.getMessage());

		verify(authManager).getCurrentUser(); // Eskiden verifyNoInteractions idi, şimdi verify(authManager)
		verifyNoInteractions(trainerRepository);
		verifyNoInteractions(userRepository);
	}
}