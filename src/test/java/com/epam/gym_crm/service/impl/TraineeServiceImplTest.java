package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

import com.epam.gym_crm.api.dto.UserCreationResult;
import com.epam.gym_crm.api.dto.request.UserActivationRequest;
import com.epam.gym_crm.api.dto.request.trainee.TraineeCreateRequest;
import com.epam.gym_crm.api.dto.request.trainee.TraineeUpdateRequest;
import com.epam.gym_crm.api.dto.request.trainee.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.api.dto.response.TraineeProfileResponse;
import com.epam.gym_crm.api.dto.response.UserRegistrationResponse;
import com.epam.gym_crm.db.entity.Trainee;
import com.epam.gym_crm.db.entity.Trainer;
import com.epam.gym_crm.db.entity.Training;
import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.TraineeRepository;
import com.epam.gym_crm.db.repository.TrainerRepository;
import com.epam.gym_crm.db.repository.TrainingRepository;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.service.IAuthenticationService;
import com.epam.gym_crm.domain.service.impl.AuthenticationInfoService;
import com.epam.gym_crm.domain.service.impl.TraineeServiceImpl;
import com.epam.gym_crm.monitoring.metric.AppMetrics;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

	@Mock
	private TraineeRepository traineeRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private TrainerRepository trainerRepository;
	@Mock
	private TrainingRepository trainingRepository;
	@Mock
	private IAuthenticationService authenticationService;
	@Mock
	private AuthenticationInfoService authenticationInfoService;
	@Mock
	private AppMetrics appMetrics;

	@InjectMocks
	private TraineeServiceImpl traineeService;

	private User testUser;
	private Trainee testTrainee;

	@BeforeEach
	void setUp() {
		testUser = new User(1L, "Test", "Trainee", "Test.Trainee", "pass", true, null, null);
		testTrainee = new Trainee(1L, LocalDate.now(), "Address", testUser, new HashSet<>(), new HashSet<>());
	}

	// --- createTrainee Tests ---
	@Test
	void shouldCreateTraineeSuccessfully() {
		TraineeCreateRequest request = new TraineeCreateRequest("New", "Trainee", null, null);
		User newUser = new User(2L, "New", "Trainee", "New.Trainee", "encoded", true, null, null);
		UserCreationResult creationResult = new UserCreationResult(newUser, "rawPassword");

		when(authenticationService.prepareUserWithCredentials(anyString(), anyString())).thenReturn(creationResult);
		when(traineeRepository.save(any(Trainee.class))).thenReturn(new Trainee(2L, null, null, newUser, null, null));
		when(authenticationService.createAccessToken(newUser)).thenReturn("dummy.token");

		UserRegistrationResponse response = traineeService.createTrainee(request);

		assertNotNull(response);
		assertEquals("New.Trainee", response.getUsername());
		assertEquals("rawPassword", response.getPassword());
		verify(appMetrics).incrementTraineeCreation();
	}

	@Test
	void shouldThrowExceptionWhenCreateRequestIsNull() {
		assertThrows(BaseException.class, () -> traineeService.createTrainee(null));
		verifyNoInteractions(authenticationService, traineeRepository);
	}

	@Test
	void shouldPropagateExceptionWhenUserCreationFails() {
		TraineeCreateRequest request = new TraineeCreateRequest("Fail", "User", null, null);
		when(authenticationService.prepareUserWithCredentials(anyString(), anyString()))
				.thenThrow(new RuntimeException("DB Error"));
		assertThrows(RuntimeException.class, () -> traineeService.createTrainee(request));
		verify(traineeRepository, never()).save(any());
	}

	// --- findTraineeById Tests ---
	@Test
	void shouldFindTraineeByIdSuccessfully() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findById(1L)).thenReturn(Optional.of(testTrainee));

		TraineeProfileResponse response = traineeService.findTraineeById(1L);

		assertNotNull(response);
		assertEquals(testUser.getUsername(), response.getUsername());
	}

	@Test
	void shouldThrowExceptionWhenFindByIdNotFound() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findById(999L)).thenReturn(Optional.empty());
		assertThrows(BaseException.class, () -> traineeService.findTraineeById(999L));
	}

	@Test
	void shouldThrowExceptionWhenFindByIdWithNullId() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		assertThrows(BaseException.class, () -> traineeService.findTraineeById(null));
		verifyNoInteractions(traineeRepository);
	}

	// --- findTraineeByUsername Tests ---
	@Test
	void shouldFindTraineeByUsernameSuccessfully() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findByUserUsername("Test.Trainee")).thenReturn(Optional.of(testTrainee));

		TraineeProfileResponse response = traineeService.findTraineeByUsername("Test.Trainee");

		assertNotNull(response);
		assertEquals(testUser.getUsername(), response.getUsername());
	}

	@Test
	void shouldThrowExceptionWhenFindByUsernameNotFound() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findByUserUsername("non.existent")).thenReturn(Optional.empty());
		assertThrows(BaseException.class, () -> traineeService.findTraineeByUsername("non.existent"));
	}

	@Test
	void shouldThrowExceptionWhenFindByUsernameWithNullOrEmpty() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		assertThrows(BaseException.class, () -> traineeService.findTraineeByUsername(null));
		assertThrows(BaseException.class, () -> traineeService.findTraineeByUsername(""));
		verifyNoInteractions(traineeRepository);
	}

	// --- getAllTrainees Tests ---
	@Test
	void shouldGetAllTraineesSuccessfully() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findAll()).thenReturn(List.of(testTrainee));
		List<TraineeProfileResponse> responses = traineeService.getAllTrainees();
		assertEquals(1, responses.size());
	}

	@Test
	void shouldReturnEmptyListWhenNoTraineesExist() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findAll()).thenReturn(Collections.emptyList());
		List<TraineeProfileResponse> responses = traineeService.getAllTrainees();
		assertTrue(responses.isEmpty());
	}

	// --- updateTrainee Tests ---
	@Test
	void shouldUpdateTraineeSuccessfully() {
		TraineeUpdateRequest request = new TraineeUpdateRequest(testUser.getUsername(), "Updated", null, null, null,
				true);
		when(authenticationInfoService.getCurrentUsername()).thenReturn(testUser.getUsername());
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
		when(traineeRepository.save(any(Trainee.class))).thenAnswer(i -> i.getArgument(0));

		TraineeProfileResponse response = traineeService.updateTrainee(request);
		assertEquals("Updated", response.getFirstName());
	}

	@Test
	void shouldThrowExceptionWhenUpdateTraineeNotFound() {
		TraineeUpdateRequest request = new TraineeUpdateRequest(testUser.getUsername(), null, null, null, null, true);
		when(authenticationInfoService.getCurrentUsername()).thenReturn(testUser.getUsername());
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.empty());
		assertThrows(BaseException.class, () -> traineeService.updateTrainee(request));
	}

	@Test
	void shouldThrowExceptionWhenUpdateRequestIsNull() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		assertThrows(BaseException.class, () -> traineeService.updateTrainee(null));
	}

	@Test
	void shouldThrowExceptionWhenUpdateUnauthorized() {
		TraineeUpdateRequest request = new TraineeUpdateRequest("Test.Trainee", null, null, null, null, true);
		when(authenticationInfoService.getCurrentUsername()).thenReturn("unauthorized.user");
		assertThrows(BaseException.class, () -> traineeService.updateTrainee(request));
	}

	// --- updateTraineeTrainersList Tests ---
	@Test
	void shouldUpdateTraineeTrainersListSuccessfully() {
		TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest(testUser.getUsername(),
				List.of("Trainer.One"));
		User trainerUser = new User(2L, "Trainer", "One", "Trainer.One", "p", true, null, null);
		Trainer trainer = new Trainer(2L, new TrainingType(), trainerUser, new HashSet<>(), new HashSet<>());

		when(authenticationInfoService.getCurrentUsername()).thenReturn(testUser.getUsername());
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUserUsername("Trainer.One")).thenReturn(Optional.of(trainer));

		traineeService.updateTraineeTrainersList(request);

		assertEquals(1, testTrainee.getTrainers().size());
		verify(trainingRepository).deleteByTraineeId(anyLong());
		verify(trainingRepository, times(1)).save(any(Training.class));
	}

	@Test
	void shouldClearTraineeTrainersListSuccessfully() {
		TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest(testUser.getUsername(),
				Collections.emptyList());
		when(authenticationInfoService.getCurrentUsername()).thenReturn(testUser.getUsername());
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));

		traineeService.updateTraineeTrainersList(request);

		assertTrue(testTrainee.getTrainers().isEmpty());
		verify(trainerRepository, never()).findByUserUsername(anyString());
	}

	@Test
	void shouldThrowExceptionWhenUpdateTrainersRequestIsNull() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		assertThrows(NullPointerException.class, () -> traineeService.updateTraineeTrainersList(null));	}

	@Test
	void shouldThrowExceptionWhenTraineeForUpdateTrainersNotFound() {
		TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest("NonExistent.Trainee",
				Collections.emptyList());
		when(authenticationInfoService.getCurrentUsername()).thenReturn("NonExistent.Trainee");
		when(traineeRepository.findByUserUsername("NonExistent.Trainee")).thenReturn(Optional.empty());
		assertThrows(BaseException.class, () -> traineeService.updateTraineeTrainersList(request));
	}

	@Test
	void shouldThrowExceptionWhenUpdateTrainersUnauthorized() {
		TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest(testUser.getUsername(),
				Collections.emptyList());
		when(authenticationInfoService.getCurrentUsername()).thenReturn("unauthorized.user");
		assertThrows(BaseException.class, () -> traineeService.updateTraineeTrainersList(request));
	}

	@Test
	void shouldThrowExceptionWhenTrainerInListNotFound() {
		TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest(testUser.getUsername(),
				List.of("NonExistent.Trainer"));
		when(authenticationInfoService.getCurrentUsername()).thenReturn(testUser.getUsername());
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUserUsername("NonExistent.Trainer")).thenReturn(Optional.empty());
		assertThrows(BaseException.class, () -> traineeService.updateTraineeTrainersList(request));
	}

	@Test
	void shouldThrowExceptionWhenTrainerInListInactive() {
		TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest(testUser.getUsername(),
				List.of("Inactive.Trainer"));
		User inactiveTrainerUser = new User();
		inactiveTrainerUser.setActive(false);
		Trainer inactiveTrainer = new Trainer();
		inactiveTrainer.setUser(inactiveTrainerUser);

		when(authenticationInfoService.getCurrentUsername()).thenReturn(testUser.getUsername());
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));
		when(trainerRepository.findByUserUsername("Inactive.Trainer")).thenReturn(Optional.of(inactiveTrainer));

		assertThrows(BaseException.class, () -> traineeService.updateTraineeTrainersList(request));
	}

	// --- activateDeactivateTrainee Tests ---
	@Test
	void shouldDeactivateTraineeSuccessfully() {
		UserActivationRequest request = new UserActivationRequest(testUser.getUsername(), false);
		testUser.setActive(true);
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		traineeService.activateDeactivateTrainee(request);

		assertFalse(testUser.isActive());
	}

	@Test
	void shouldActivateTraineeSuccessfully() {
		UserActivationRequest request = new UserActivationRequest(testUser.getUsername(), true);
		testUser.setActive(false);
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));
		when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

		traineeService.activateDeactivateTrainee(request);

		assertTrue(testUser.isActive());
	}

	@Test
	void shouldNotChangeActivationStatusIfAlreadyInRequestedState() {
		UserActivationRequest request = new UserActivationRequest(testUser.getUsername(), true);
		testUser.setActive(true);
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));

		traineeService.activateDeactivateTrainee(request);

		verify(userRepository, never()).save(any());
	}

	@Test
	void shouldThrowExceptionWhenTraineeForActivationNotFound() {
		UserActivationRequest request = new UserActivationRequest("non.existent", true);
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findByUserUsername("non.existent")).thenReturn(Optional.empty());
		assertThrows(BaseException.class, () -> traineeService.activateDeactivateTrainee(request));
	}

	@Test
	void shouldThrowExceptionWhenActivationRequestIsNull() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		assertThrows(NullPointerException.class, () -> traineeService.activateDeactivateTrainee(null));
	}

	// --- deleteTraineeById Tests ---
	@Test
	void shouldDeleteTraineeByIdSuccessfully() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn(testUser.getUsername());
		when(traineeRepository.findById(1L)).thenReturn(Optional.of(testTrainee));

		traineeService.deleteTraineeById(1L);

		verify(traineeRepository).delete(testTrainee);
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByIdNotFound() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		when(traineeRepository.findById(999L)).thenReturn(Optional.empty());
		assertThrows(BaseException.class, () -> traineeService.deleteTraineeById(999L));
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByIdWithNullId() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		assertThrows(BaseException.class, () -> traineeService.deleteTraineeById(null));
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByIdUnauthorized() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("unauthorized.user");
		when(traineeRepository.findById(1L)).thenReturn(Optional.of(testTrainee));
		assertThrows(BaseException.class, () -> traineeService.deleteTraineeById(1L));
	}

	// --- deleteTraineeByUsername Tests ---
	@Test
	void shouldDeleteTraineeByUsernameSuccessfully() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn(testUser.getUsername());
		when(traineeRepository.findByUserUsername(testUser.getUsername())).thenReturn(Optional.of(testTrainee));

		traineeService.deleteTraineeByUsername(testUser.getUsername());

		verify(traineeRepository).delete(testTrainee);
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByUsernameNotFound() {
		String usernameToDelete = "non.existent";
		when(authenticationInfoService.getCurrentUsername()).thenReturn(usernameToDelete); 
																							
		when(traineeRepository.findByUserUsername(usernameToDelete)).thenReturn(Optional.empty());
		assertThrows(BaseException.class, () -> traineeService.deleteTraineeByUsername(usernameToDelete));
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByUsernameWithNullOrEmpty() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("any.user");
		assertThrows(BaseException.class, () -> traineeService.deleteTraineeByUsername(null));
		assertThrows(BaseException.class, () -> traineeService.deleteTraineeByUsername(""));
	}

	@Test
	void shouldThrowExceptionWhenDeleteTraineeByUsernameUnauthorized() {
		when(authenticationInfoService.getCurrentUsername()).thenReturn("unauthorized.user");
		assertThrows(BaseException.class, () -> traineeService.deleteTraineeByUsername(testUser.getUsername()));
	}
}