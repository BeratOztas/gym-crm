package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym_crm.auth.AuthManager;
import com.epam.gym_crm.db.entity.Trainee;
import com.epam.gym_crm.db.entity.Trainer;
import com.epam.gym_crm.db.entity.Training;
import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.TraineeRepository;
import com.epam.gym_crm.db.repository.TrainerRepository;
import com.epam.gym_crm.db.repository.TrainingRepository;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeCreateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateRequest;
import com.epam.gym_crm.dto.request.trainee.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.dto.response.TraineeProfileResponse;
import com.epam.gym_crm.dto.response.TrainerInfoResponse;
import com.epam.gym_crm.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.monitoring.metrics.AppMetrics;
import com.epam.gym_crm.service.IAuthenticationService;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

	@Mock
	private TraineeRepository traineeRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private TrainerRepository trainerRepository;
	@Mock
	private IAuthenticationService authenticationService;
	@Mock
	private AuthManager authManager;
	@Mock
	private TrainingRepository trainingRepository;

	@InjectMocks
	private TraineeServiceImpl traineeService;
	
	@Mock 
	private AppMetrics appMetrics;

	private User testUser;
	private Trainee testTrainee;
	private TraineeCreateRequest createRequest;
	private TraineeUpdateRequest updateRequest;
	private UserActivationRequest activationRequest;

	private User trainerUser1;
	private Trainer trainer1;
	private User trainerUser2;
	private Trainer trainer2;
	private TrainingType strengthTrainingType;
	private TrainingType cardioTrainingType;

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(1L);
		testUser.setFirstName("Test");
		testUser.setLastName("Trainee");
		testUser.setUsername("Test.Trainee");
		testUser.setPassword("password123");
		testUser.setActive(true);

		testTrainee = new Trainee();
		testTrainee.setId(1L);
		testTrainee.setUser(testUser);
		testTrainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
		testTrainee.setAddress("Test Address");
		testTrainee.setTrainers(new HashSet<>());

		createRequest = new TraineeCreateRequest();
		createRequest.setFirstName("New");
		createRequest.setLastName("Trainee");
		createRequest.setDateOfBirth(LocalDate.of(1995, 5, 10));
		createRequest.setAddress("New Trainee Address");

		updateRequest = new TraineeUpdateRequest();
		updateRequest.setUsername("Test.Trainee");
		updateRequest.setFirstName("Updated");
		updateRequest.setLastName("Trainee");
		updateRequest.setDateOfBirth(LocalDate.of(1996, 6, 11));
		updateRequest.setAddress("Updated Address");

		activationRequest = new UserActivationRequest();
		activationRequest.setUsername("Test.Trainee");
		activationRequest.setIsActive(false);

		this.traineeService = new TraineeServiceImpl(traineeRepository, authenticationService, authManager,
				userRepository, trainerRepository, trainingRepository,appMetrics);

		testUser = new User(1L, "Test", "User", "test.user", "pass123", true, null, null);
		testTrainee = new Trainee(10L, LocalDate.of(2000, 1, 1), "Some Address", testUser, new HashSet<>(),
				new HashSet<>());

		strengthTrainingType = new TrainingType(20L, "Strength");
		cardioTrainingType = new TrainingType(21L, "Cardio");

		trainerUser1 = new User(30L, "Trainer", "One", "Trainer.One", "passOne", true, null, null);
		trainer1 = new Trainer(31L, strengthTrainingType, trainerUser1, new HashSet<>(), new HashSet<>());

		trainerUser2 = new User(40L, "Trainer", "Two", "Trainer.Two", "passTwo", true, null, null);
		trainer2 = new Trainer(41L, cardioTrainingType, trainerUser2, new HashSet<>(), new HashSet<>());
	}

	// --- createTrainee Tests ---

	@Test
	void shouldCreateTraineeSuccessfully() {

		User newUser = new User();
		newUser.setId(2L);
		newUser.setFirstName(createRequest.getFirstName());
		newUser.setLastName(createRequest.getLastName());
		newUser.setUsername("New.Trainee");
		newUser.setPassword("generatedPass");
		newUser.setActive(true);

		Trainee newTrainee = new Trainee();
		newTrainee.setId(2L);
		newTrainee.setUser(newUser);
		newTrainee.setDateOfBirth(createRequest.getDateOfBirth());
		newTrainee.setAddress(createRequest.getAddress());
		newTrainee.setTrainers(new HashSet<>());

		// Çünkü servis metodu 'false' ile çağırıyor.
		when(authenticationService.createAndSaveUser(createRequest.getFirstName(), createRequest.getLastName()))
				.thenReturn(newUser);

		when(traineeRepository.save(any(Trainee.class))).thenReturn(newTrainee);

		// When
		UserRegistrationResponse response = traineeService.createTrainee(createRequest);

		// Then
		assertNotNull(response);
		assertEquals(newUser.getUsername(), response.getUsername());

		verify(authenticationService).createAndSaveUser(createRequest.getFirstName(), createRequest.getLastName());
		verify(traineeRepository).save(any(Trainee.class));
	}

	@Test
	void shouldThrowExceptionWhenCreateRequestIsNull() {
		BaseException exception = assertThrows(BaseException.class, () -> traineeService.createTrainee(null));
		assertTrue(exception.getMessage().contains("Trainee can not be null"));
		verifyNoInteractions(authenticationService);
		verifyNoInteractions(traineeRepository);
	}

	@Test
	void shouldPropagateExceptionWhenUserCreationFails() {
		when(authenticationService.createAndSaveUser(anyString(), anyString()))
				.thenThrow(new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "User creation failed")));

		BaseException exception = assertThrows(BaseException.class, () -> traineeService.createTrainee(createRequest));
		assertTrue(exception.getMessage().contains("User creation failed"));
		verify(authenticationService).createAndSaveUser(anyString(), anyString());
		verify(traineeRepository, never()).save(any(Trainee.class));
	}

	// --- findTraineeById Tests ---

	@Test
	void shouldFindTraineeByIdSuccessfully() {

		when(authManager.getCurrentUser()).thenReturn(testUser);

		when(traineeRepository.findById(testTrainee.getId())).thenReturn(Optional.of(testTrainee));

		TraineeProfileResponse response = traineeService.findTraineeById(testTrainee.getId());

		// Then
		assertNotNull(response);
		assertEquals(testTrainee.getUser().getUsername(), response.getUsername());

		verify(authManager).getCurrentUser(); // Added this verify

		verify(traineeRepository).findById(testTrainee.getId());
	}

	@Test
	void shouldThrowExceptionWhenFindByIdNotFound() {
		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findById(999L)).thenReturn(Optional.empty());
		BaseException exception = assertThrows(BaseException.class, () -> traineeService.findTraineeById(999L));

		assertTrue(exception.getMessage().contains("Trainee not found with ID: 999"));
		verify(authManager).getCurrentUser();
		verify(traineeRepository).findById(999L);
	}

	@Test
	void shouldThrowExceptionWhenFindByIdWithNullId() {
		when(authManager.getCurrentUser()).thenReturn(testUser); // Servis metodu authManager kullanıyor
		BaseException exception = assertThrows(BaseException.class, () -> traineeService.findTraineeById(null));

		assertTrue(
				exception.getMessage().contains("Trainee ID for lookup must be a positive value. Provided ID: null"));
		verify(authManager).getCurrentUser();
		verifyNoInteractions(traineeRepository);
	}

	// --- findTraineeByUsername Tests ---

	@Test
	void shouldFindTraineeByUsernameSuccessfully() {

		when(authManager.getCurrentUser()).thenReturn(testUser);

		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));

		// When
		TraineeProfileResponse response = traineeService.findTraineeByUsername(testUser.getUsername());

		// Then
		assertNotNull(response);
		assertEquals(testUser.getUsername(), response.getUsername());

		verify(authManager).getCurrentUser();

		verify(traineeRepository).findByUserUsername(testUser.getUsername());
	}

	@Test
	void shouldThrowExceptionWhenFindByUsernameNotFound() {
		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername("NonExistent.User")).thenReturn(Optional.empty());
		BaseException exception = assertThrows(BaseException.class,
				() -> traineeService.findTraineeByUsername("NonExistent.User"));

		assertTrue(exception.getMessage().contains("Trainee not found with username: NonExistent.User"));
		verify(authManager).getCurrentUser(); // Kullanılıyorsa verify et
		verify(traineeRepository).findByUserUsername("NonExistent.User");
	}

	@Test
	void shouldThrowExceptionWhenFindByUsernameWithNullOrEmpty() {

		when(authManager.getCurrentUser()).thenReturn(new User());

		// When & Then (Testing for null username)
		BaseException exceptionNull = assertThrows(BaseException.class,
				() -> traineeService.findTraineeByUsername(null));
		// The expected message includes the prefix from MessageType.INVALID_ARGUMENT
		assertEquals("Invalid parameter provided. : Trainee username for lookup must not be null or empty.",
				exceptionNull.getMessage());

		// When & Then (Testing for empty username)
		BaseException exceptionEmpty = assertThrows(BaseException.class,
				() -> traineeService.findTraineeByUsername(""));
		// The expected message also includes the prefix
		assertEquals("Invalid parameter provided. : Trainee username for lookup must not be null or empty.",
				exceptionEmpty.getMessage());

		verify(authManager, times(2)).getCurrentUser();

		verifyNoInteractions(traineeRepository);
		verifyNoInteractions(userRepository);
	}

	// --- getAllTrainees Tests ---

	@Test
	void shouldGetAllTraineesSuccessfully() {

		when(authManager.getCurrentUser()).thenReturn(testUser);

		Trainee trainee2 = new Trainee();
		User user2 = new User();
		user2.setUsername("user2");
		trainee2.setUser(user2);
		trainee2.setTrainers(new HashSet<>());

		List<Trainee> trainees = Arrays.asList(testTrainee, trainee2);
		when(traineeRepository.findAll()).thenReturn(trainees);

		// When
		List<TraineeProfileResponse> responses = traineeService.getAllTrainees();

		// Then
		assertNotNull(responses);
		assertEquals(2, responses.size());
		assertEquals(testTrainee.getUser().getUsername(), responses.get(0).getUsername());
		assertEquals(trainee2.getUser().getUsername(), responses.get(1).getUsername());

		verify(authManager).getCurrentUser();

		verify(traineeRepository).findAll();
	}

	@Test
	void shouldReturnEmptyListWhenNoTraineesExist() {

		when(authManager.getCurrentUser()).thenReturn(testUser);

		when(traineeRepository.findAll()).thenReturn(new ArrayList<>());

		// When
		List<TraineeProfileResponse> responses = traineeService.getAllTrainees();

		// Then
		assertNotNull(responses);
		assertTrue(responses.isEmpty());

		verify(authManager).getCurrentUser();

		verify(traineeRepository).findAll();
	}

	// --- updateTrainee Tests ---

	@Test
	void shouldUpdateTraineeSuccessfully() {
		// Given
		User existingUser = new User();
		existingUser.setId(1L);
		existingUser.setUsername("Test.Trainee");
		existingUser.setFirstName("Old");
		existingUser.setLastName("Trainee");
		existingUser.setPassword("oldPass");
		existingUser.setActive(true);

		Trainee existingTrainee = new Trainee();
		existingTrainee.setId(1L);
		existingTrainee.setUser(existingUser);
		existingTrainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
		existingTrainee.setAddress("Old Address");
		existingTrainee.setTrainers(new HashSet<>()); // Initialize trainers set

		when(authManager.getCurrentUser()).thenReturn(existingUser);
		when(traineeRepository.findByUserUsername(updateRequest.getUsername()))
				.thenReturn(Optional.of(existingTrainee));
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
		when(traineeRepository.save(any(Trainee.class))).thenAnswer(i -> i.getArgument(0));

		// When
		TraineeProfileResponse response = traineeService.updateTrainee(updateRequest);

		// Then
		assertNotNull(response);
		assertEquals(updateRequest.getUsername(), response.getUsername());
		assertEquals(updateRequest.getFirstName(), response.getFirstName());
		assertEquals(updateRequest.getLastName(), response.getLastName());
		assertEquals(updateRequest.getDateOfBirth(), response.getDateOfBirth());
		assertEquals(updateRequest.getAddress(), response.getAddress());

		verify(authManager).getCurrentUser();
		verify(userRepository).save(existingUser);
		verify(traineeRepository).save(existingTrainee);
	}

	@Test
	void shouldThrowExceptionWhenUpdateTraineeNotFound() {
		// 1. Arrange - Gerekli koşulları hazırla
		when(authManager.getCurrentUser()).thenReturn(testUser);

		TraineeUpdateRequest updateRequest = new TraineeUpdateRequest();
		updateRequest.setUsername(testUser.getUsername());

		when(traineeRepository.findByUserUsername(updateRequest.getUsername())).thenReturn(Optional.empty());

		BaseException exception = assertThrows(BaseException.class, () -> traineeService.updateTrainee(updateRequest));

		assertNotNull(exception);

		assertTrue(exception.getMessage().contains("Trainee profile not found."));

		verify(authManager).getCurrentUser();
		
		verify(traineeRepository).findByUserUsername(updateRequest.getUsername());
		verify(userRepository, never()).save(any(User.class));
		verify(traineeRepository, never()).save(any(Trainee.class));
	}

	@Test
	void shouldThrowExceptionWhenUpdateRequestIsNull() {

		when(authManager.getCurrentUser()).thenReturn(new User()); // Provide a dummy User object

		// When & Then
		Exception thrownException = assertThrows(BaseException.class, () -> traineeService.updateTrainee(null));

		// Assert the exception message as we've refined it
		assertEquals("Invalid parameter provided. : Update request or username must not be null/empty.",
				thrownException.getMessage());

		verify(authManager).getCurrentUser(); 

		// Other interactions should still not happen if the null check occurs early.
		verifyNoInteractions(traineeRepository);
		verifyNoInteractions(userRepository);
	}

	@Test
	void shouldThrowExceptionWhenUpdateUnauthorized() {
		// Given
		User unauthorizedUser = new User();
		unauthorizedUser.setUsername("Unauthorized.User"); // Create an unauthorized user

		// Mock the current user to be the unauthorized user
		when(authManager.getCurrentUser()).thenReturn(unauthorizedUser);

		TraineeUpdateRequest updateRequest = new TraineeUpdateRequest();
		updateRequest.setUsername(testUser.getUsername()); // "Test.Trainee"

		// When & Then
		// Call the service with the unauthorized user trying to update another trainee
		BaseException exception = assertThrows(BaseException.class, () -> traineeService.updateTrainee(updateRequest));

		// Assert that the exception is an authorization error
		assertEquals("You are not authorized. : You are not authorized to update this Trainee profile.",
				exception.getMessage());

		// Verifications
		// Verify that getCurrentUser was called
		verify(authManager).getCurrentUser();

		// Verify that findByUserUsername was *never* called in this unauthorized
		// scenario
		verify(traineeRepository, never()).findByUserUsername(anyString());

		// Verify that save methods were never called
		verify(userRepository, never()).save(any(User.class));
		verify(traineeRepository, never()).save(any(Trainee.class));
	}

	@Test
	void shouldUpdateTraineeTrainersListSuccessfully() {
		// Given
		TraineeUpdateTrainersRequest req = new TraineeUpdateTrainersRequest();
		req.setTraineeUsername(testUser.getUsername());
		req.setTrainerUsernames(Arrays.asList("Trainer.One", "Trainer.Two"));

		// *** MOCKITO DAVRANIŞLARI ***
		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername(req.getTraineeUsername())).thenReturn(Optional.of(testTrainee));

		when(trainerRepository.findByUserUsername(anyString())).thenAnswer(invocation -> {
			String username = invocation.getArgument(0);
			if ("Trainer.One".equals(username)) {
				return Optional.of(trainer1);
			}
			if ("Trainer.Two".equals(username)) {
				return Optional.of(trainer2);
			}
			return Optional.empty();
		});

		when(traineeRepository.save(any(Trainee.class))).thenAnswer(i -> {
			Trainee savedTrainee = i.getArgument(0);
			savedTrainee.setTrainers(new HashSet<>(Arrays.asList(trainer1, trainer2)));
			return savedTrainee;
		});

		when(trainingRepository.save(any(Training.class))).thenAnswer(i -> i.getArgument(0));

		// When
		List<TrainerInfoResponse> response = traineeService.updateTraineeTrainersList(req);

		// Then
		assertNotNull(response);
		assertEquals(2, response.size());

		Set<String> returnedUsernames = response.stream().map(TrainerInfoResponse::getUsername)
				.collect(Collectors.toSet());
		Set<String> expectedUsernames = new HashSet<>(Arrays.asList("Trainer.One", "Trainer.Two"));
		assertEquals(expectedUsernames, returnedUsernames);

		// *** MOCKITO DOĞRULAMALARI ***
		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(req.getTraineeUsername());
		// findByUsername'in 2 kez çağrıldığını doğru şekilde doğrula
		verify(trainerRepository, times(2)).findByUserUsername(anyString());
		verify(traineeRepository).save(testTrainee);
		// deleteByTraineeId'in çağrıldığını doğrula
		verify(trainingRepository).deleteByTraineeId(testTrainee.getId());
		// save metodunun 2 kez çağrıldığını doğrula (her bir yeni eğitmen için)
		verify(trainingRepository, times(2)).save(any(Training.class));
	}

	@Test
	void shouldClearTraineeTrainersListSuccessfully() {
		// Given
		TraineeUpdateTrainersRequest req = new TraineeUpdateTrainersRequest();
		req.setTraineeUsername(testUser.getUsername());
		req.setTrainerUsernames(new ArrayList<>()); 

		User existingTrainerUser = new User();
		existingTrainerUser.setUsername("Existing.Trainer");
		Trainer existingTrainer = new Trainer();
		existingTrainer.setUser(existingTrainerUser);

		testTrainee.setTrainers(new HashSet<>(Arrays.asList(existingTrainer))); 
		testTrainee.setUser(testUser); // Trainee nesnesine User'ı ekledik

		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername(req.getTraineeUsername())).thenReturn(Optional.of(testTrainee));
		when(traineeRepository.save(any(Trainee.class))).thenAnswer(i -> {
			Trainee savedTrainee = i.getArgument(0);
			savedTrainee.setTrainers(new HashSet<>()); // Save işlemi sonucunda boş listeyi set et
			return savedTrainee;
		});

		// When
		List<TrainerInfoResponse> response = traineeService.updateTraineeTrainersList(req);

		// Then
		assertNotNull(response);
		assertTrue(response.isEmpty()); 

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(req.getTraineeUsername());
		verify(trainerRepository, never()).findByUserUsername(anyString()); 
		verify(traineeRepository).save(testTrainee);
	}

	// --- Other tests ---

	@Test
	void shouldThrowExceptionWhenUpdateTrainersRequestIsNull() {
		
		when(authManager.getCurrentUser()).thenReturn(new User());

		// traineeService.updateTraineeTrainersList(null) çağrıldığında
		// NullPointerException fırlatılmasını bekle ve yakala
		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> traineeService.updateTraineeTrainersList(null));

		assertNotNull(exception);

		// authManager'ın getCurrentUser metodunun çağrıldığını doğrula
		verify(authManager).getCurrentUser();

		// doğrula
		verifyNoInteractions(traineeRepository);
		verifyNoInteractions(userRepository);
	}

	@Test
	void shouldThrowExceptionWhenTraineeForUpdateTrainersNotFound() {

		TraineeUpdateTrainersRequest req = new TraineeUpdateTrainersRequest();
		req.setTraineeUsername("NonExistent.Trainee");
		req.setTrainerUsernames(Arrays.asList("Trainer.One"));

		User authorizedButNonExistentTargetUser = new User();
		authorizedButNonExistentTargetUser.setUsername(req.getTraineeUsername());

		when(authManager.getCurrentUser()).thenReturn(authorizedButNonExistentTargetUser);
		when(traineeRepository.findByUserUsername(req.getTraineeUsername())).thenReturn(Optional.empty());

		// When & Then
		BaseException exception = assertThrows(BaseException.class,
				() -> traineeService.updateTraineeTrainersList(req));

		assertEquals("Resource not found. : Trainee with username " + req.getTraineeUsername() + " not found.",
				exception.getMessage());

		// Verifications
		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(req.getTraineeUsername());
		verifyNoInteractions(trainerRepository);
		verify(traineeRepository, never()).save(any(Trainee.class));
	}

	@Test
	void shouldThrowExceptionWhenUpdateTrainersUnauthorized() {
		// Given
		User unauthorizedUser = new User();
		unauthorizedUser.setUsername("Unauthorized.User");

		when(authManager.getCurrentUser()).thenReturn(unauthorizedUser);

		TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest();
		request.setTraineeUsername(testUser.getUsername());

		// When & Then
		BaseException exception = assertThrows(BaseException.class,
				() -> traineeService.updateTraineeTrainersList(request));

		assertEquals("You are not authorized. : You are not authorized to update trainers list for other trainees.",
				exception.getMessage());

		// Verifications
		verify(authManager).getCurrentUser();
		verifyNoInteractions(traineeRepository);
		verifyNoInteractions(userRepository); 
	}

	@Test
	void shouldThrowExceptionWhenTrainerInListNotFound() {
		TraineeUpdateTrainersRequest req = new TraineeUpdateTrainersRequest();
		req.setTraineeUsername(testUser.getUsername());
		req.setTrainerUsernames(Arrays.asList("Trainer.One", "NonExistent.Trainer"));

		User trainerUser1 = new User();
		trainerUser1.setUsername("Trainer.One");
		trainerUser1.setActive(true);
		Trainer trainer1 = new Trainer();
		trainer1.setUser(trainerUser1);

		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername(req.getTraineeUsername())).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUserUsername("Trainer.One")).thenReturn(Optional.of(trainer1));
		when(trainerRepository.findByUserUsername("NonExistent.Trainer")).thenReturn(Optional.empty());

		BaseException exception = assertThrows(BaseException.class,
				() -> traineeService.updateTraineeTrainersList(req));

		assertTrue(exception.getMessage().contains("Trainer with username NonExistent.Trainer not found."));
		verify(trainerRepository).findByUserUsername("NonExistent.Trainer");
		verify(traineeRepository, never()).save(any(Trainee.class));
	}

	@Test
	void shouldThrowExceptionWhenTrainerInListInactive() {
		TraineeUpdateTrainersRequest req = new TraineeUpdateTrainersRequest();
		req.setTraineeUsername(testUser.getUsername());
		req.setTrainerUsernames(Arrays.asList("Inactive.Trainer"));

		User inactiveTrainerUser = new User();
		inactiveTrainerUser.setUsername("Inactive.Trainer");
		inactiveTrainerUser.setActive(false);
		Trainer inactiveTrainer = new Trainer();
		inactiveTrainer.setUser(inactiveTrainerUser);

		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername(req.getTraineeUsername())).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUserUsername("Inactive.Trainer")).thenReturn(Optional.of(inactiveTrainer));

		BaseException exception = assertThrows(BaseException.class,
				() -> traineeService.updateTraineeTrainersList(req));

		assertTrue(exception.getMessage().contains("Trainer Inactive.Trainer is not active. Cannot assign."));
		verify(trainerRepository).findByUserUsername("Inactive.Trainer");
		verify(traineeRepository, never()).save(any(Trainee.class));
	}

	@Test
	void shouldThrowExceptionWhenTraineeForUpdateTrainersIsInactive() {
		// Given
		TraineeUpdateTrainersRequest req = new TraineeUpdateTrainersRequest();
		req.setTraineeUsername(testUser.getUsername());
		req.setTrainerUsernames(Arrays.asList("Trainer.One"));

		testUser.setActive(false); // Make the trainee's user inactive
		testTrainee.setUser(testUser); // Update the testTrainee with the inactive user

		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername(req.getTraineeUsername())).thenReturn(Optional.of(testTrainee));

		// When & Then
		BaseException exception = assertThrows(BaseException.class,
				() -> traineeService.updateTraineeTrainersList(req));

		assertTrue(exception.getMessage().contains(
				"Trainee " + req.getTraineeUsername() + " is not active. Cannot update their trainers list."));

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(req.getTraineeUsername());
		verifyNoInteractions(trainerRepository);
		verify(traineeRepository, never()).save(any(Trainee.class));
	}

	// --- activateDeactivateTrainee Tests ---

	@Test
	void shouldActivateTraineeSuccessfully() {
		activationRequest.setIsActive(true);
		testTrainee.getUser().setActive(false);

		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername(activationRequest.getUsername()))
				.thenReturn(Optional.of(testTrainee));
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0)); 
																						

		traineeService.activateDeactivateTrainee(activationRequest);

		// testTrainee'nin user'ının aktifliği kontrol ediliyor
		assertTrue(testTrainee.getUser().isActive());

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(activationRequest.getUsername());
		verify(userRepository).save(testTrainee.getUser());
		verify(traineeRepository).save(testTrainee);
	}

	@Test
	void shouldDeactivateTraineeSuccessfully() {
		activationRequest.setIsActive(false);
		testTrainee.getUser().setActive(true);

		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername(activationRequest.getUsername()))
				.thenReturn(Optional.of(testTrainee));
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		traineeService.activateDeactivateTrainee(activationRequest);

		assertFalse(testTrainee.getUser().isActive());

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(activationRequest.getUsername());
		verify(userRepository).save(testTrainee.getUser());
		verify(traineeRepository).save(testTrainee);
	}

	@Test
	void shouldNotChangeActivationStatusIfAlreadyInRequestedState() {
		// Durum zaten aktif, istek de aktif
		activationRequest.setIsActive(true);
		testTrainee.getUser().setActive(true);

		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername(activationRequest.getUsername()))
				.thenReturn(Optional.of(testTrainee));

		traineeService.activateDeactivateTrainee(activationRequest);

		assertTrue(testTrainee.getUser().isActive());

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(activationRequest.getUsername());
		verify(userRepository, never()).save(any(User.class)); // save çağrılmamalı
		verify(traineeRepository, never()).save(any(Trainee.class)); // save çağrılmamalı
	}

	@Test
	void shouldThrowExceptionWhenTraineeForActivationNotFound() {
		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername(activationRequest.getUsername())).thenReturn(Optional.empty());

		BaseException exception = assertThrows(BaseException.class,
				() -> traineeService.activateDeactivateTrainee(activationRequest));

		assertTrue(exception.getMessage()
				.contains("Trainee with username '" + activationRequest.getUsername() + "' not found."));

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(activationRequest.getUsername());
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void shouldThrowExceptionWhenActivationRequestIsNull() {

		when(authManager.getCurrentUser()).thenReturn(new User());

		// When & Then

		NullPointerException exception = assertThrows(NullPointerException.class,
				() -> traineeService.activateDeactivateTrainee(null));

		assertTrue(exception.getMessage().contains(
				"Cannot invoke \"com.epam.gym_crm.dto.request.UserActivationRequest.getUsername()\" because \"request\" is null"));

		// authManager.getCurrentUser() çağrısının yapıldığını doğruluyoruz.
		verify(authManager).getCurrentUser();

		verifyNoInteractions(traineeRepository);
		verifyNoInteractions(userRepository);
	}

	// --- deleteTraineeById Tests ---

	@Test
	void shouldDeleteTraineeByIdSuccessfully() {
		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findById(testTrainee.getId())).thenReturn(Optional.of(testTrainee));
		doNothing().when(traineeRepository).delete(testTrainee);

		traineeService.deleteTraineeById(testTrainee.getId());

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findById(testTrainee.getId());
		verify(traineeRepository).delete(testTrainee);
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByIdNotFound() {
		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findById(999L)).thenReturn(Optional.empty());

		BaseException exception = assertThrows(BaseException.class, () -> traineeService.deleteTraineeById(999L));
		// Servis kodunuzdaki mesaj: "No trainee found to delete with ID: " + id
		assertTrue(exception.getMessage().contains("No trainee found to delete with ID: 999"));

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findById(999L);
		verify(traineeRepository, never()).delete(any(Trainee.class));
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByIdWithNullId() {
		when(authManager.getCurrentUser()).thenReturn(testUser); // Servis metodu authManager kullanıyor
		BaseException exception = assertThrows(BaseException.class, () -> traineeService.deleteTraineeById(null));
		// Servis kodunuzdaki mesaj: "Trainee ID for deletion must be a positive value.
		// Provided ID: " + id
		assertTrue(
				exception.getMessage().contains("Trainee ID for deletion must be a positive value. Provided ID: null"));
		verify(authManager).getCurrentUser(); // Kullanılıyorsa verify et
		verifyNoInteractions(traineeRepository);
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByIdUnauthorized() {
		User unauthorizedUser = new User();
		unauthorizedUser.setUsername("Unauthorized.User");

		when(authManager.getCurrentUser()).thenReturn(unauthorizedUser);
		when(traineeRepository.findById(testTrainee.getId())).thenReturn(Optional.of(testTrainee));

		BaseException exception = assertThrows(BaseException.class,
				() -> traineeService.deleteTraineeById(testTrainee.getId()));
		// Servis kodunuzdaki mesaj: "You are not authorized to delete this Trainee
		// profile."
		assertTrue(exception.getMessage().contains("You are not authorized to delete this Trainee profile."));

		verify(authManager).getCurrentUser();
		verify(traineeRepository).findById(testTrainee.getId());
		verify(traineeRepository, never()).delete(any(Trainee.class));
	}

	// --- deleteTraineeByUsername Tests ---

	@Test
	void shouldDeleteTraineeByUsernameSuccessfully() {
		when(authManager.getCurrentUser()).thenReturn(testUser);
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));
		doNothing().when(traineeRepository).delete(testTrainee);

		traineeService.deleteTraineeByUsername(testUser.getUsername());

		// Servis kodunuzda checkAuthentication() doğrudan çağrılmıyor
		verify(authManager).getCurrentUser();
		verify(traineeRepository).findByUserUsername(testUser.getUsername());
		verify(traineeRepository).delete(testTrainee);
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByUsernameNotFound() {
		when(authManager.getCurrentUser()).thenReturn(testUser); // testUser'ın username'i "Test.Trainee"

		String usernameToDelete = "Another.Trainee";

		// Metodu, yetkisiz bir kullanıcının (testUser) başka bir kullanıcıyı silmeye
		// çalıştığı senaryo ile çağırıyoruz.
		BaseException exception = assertThrows(BaseException.class,
				() -> traineeService.deleteTraineeByUsername(usernameToDelete));

		// Beklenen hata: Yetkilendirme hatası
		assertEquals("You are not authorized. : You are not authorized to delete this Trainee profile.",
				exception.getMessage());

		// Verifications
		verify(authManager).getCurrentUser();

		verify(traineeRepository, never()).findByUserUsername(anyString()); // Bu satır zaten doğruydu
		verify(traineeRepository, never()).delete(any(Trainee.class)); // Bu satır da doğruydu
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByUsernameWithNullOrEmpty() {

		when(authManager.getCurrentUser()).thenReturn(new User());

		// When & Then (Testing for null username)
		BaseException exceptionNull = assertThrows(BaseException.class,
				() -> traineeService.deleteTraineeByUsername(null));

		assertEquals("Invalid parameter provided. : Username must not be null or empty.", exceptionNull.getMessage());

		// When & Then (Testing for empty username)
		BaseException exceptionEmpty = assertThrows(BaseException.class,
				() -> traineeService.deleteTraineeByUsername(""));

		assertEquals("Invalid parameter provided. : Username must not be null or empty.", exceptionEmpty.getMessage());

		verify(authManager, times(2)).getCurrentUser();

		verifyNoInteractions(traineeRepository);
		verifyNoInteractions(userRepository);
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByUsernameUnauthorized() {
		// Given
		User unauthorizedUser = new User();
		unauthorizedUser.setUsername("Unauthorized.User"); // Yetkisiz kullanıcı

		when(authManager.getCurrentUser()).thenReturn(unauthorizedUser); // Yetkisiz kullanıcı dönsün

		BaseException exception = assertThrows(BaseException.class,
				() -> traineeService.deleteTraineeByUsername(testUser.getUsername()));

		// Beklenen hata: Yetkilendirme hatası
		assertEquals("You are not authorized. : You are not authorized to delete this Trainee profile.",
				exception.getMessage());

		// Verifications
		verify(authManager).getCurrentUser();

		verify(traineeRepository, never()).findByUserUsername(anyString());
		verify(traineeRepository, never()).delete(any(Trainee.class));
	}
}