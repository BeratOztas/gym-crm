package com.epam.gym_crm.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.epam.gym_crm.dto.request.TraineeCreateRequest;
import com.epam.gym_crm.dto.request.TraineeTrainingListRequest;
import com.epam.gym_crm.dto.request.TraineeUpdateRequest;
import com.epam.gym_crm.dto.request.TraineeUpdateTrainersRequest;
import com.epam.gym_crm.dto.request.TrainerCreateRequest;
import com.epam.gym_crm.dto.request.TrainerTrainingListRequest;
import com.epam.gym_crm.dto.request.TrainerUpdateRequest;
import com.epam.gym_crm.dto.request.TrainingCreateRequest;
import com.epam.gym_crm.dto.request.TrainingUpdateRequest;
import com.epam.gym_crm.dto.request.UserActivationRequest;
import com.epam.gym_crm.dto.response.TraineeResponse;
import com.epam.gym_crm.dto.response.TrainerResponse;
import com.epam.gym_crm.dto.response.TrainingResponse;
import com.epam.gym_crm.service.ITraineeService;
import com.epam.gym_crm.service.ITrainerService;
import com.epam.gym_crm.service.ITrainingService;

class GymCRMFacadeTest {

	@Mock
	private ITraineeService traineeService;
	@Mock
	private ITrainerService trainerService;
	@Mock
	private ITrainingService trainingService;

	@InjectMocks
	private GymCRMFacade gymCRMFacade;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void createTrainee_Success() {
		// Hazırlık
		TraineeCreateRequest request = new TraineeCreateRequest("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St",
				true);
		TraineeResponse expectedResponse = new TraineeResponse();
		expectedResponse.setFirstName("John");
		expectedResponse.setLastName("Doe");
		expectedResponse.setUsername("John.Doe");

		when(traineeService.createTrainee(request)).thenReturn(expectedResponse);

		// Çağrı
		TraineeResponse actualResponse = gymCRMFacade.createTrainee(request);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals("John.Doe", actualResponse.getUsername());
		verify(traineeService, times(1)).createTrainee(request);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void findTraineeById_Success() {
		// Hazırlık
		Long id = 1L;
		TraineeResponse expectedResponse = new TraineeResponse();
		expectedResponse.setUsername("test.trainee");

		when(traineeService.findTraineeById(id)).thenReturn(expectedResponse);

		// Çağrı
		TraineeResponse actualResponse = gymCRMFacade.findTraineeById(id);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals("test.trainee", actualResponse.getUsername());
		verify(traineeService, times(1)).findTraineeById(id);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void findTraineeById_Failure_NotFound() {
		// Hazırlık
		Long id = 99L;
		when(traineeService.findTraineeById(id)).thenThrow(new RuntimeException("Trainee not found"));

		// Çağrı ve Doğrulama
		assertThrows(RuntimeException.class, () -> gymCRMFacade.findTraineeById(id));
		verify(traineeService, times(1)).findTraineeById(id);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void findTraineeByUsername_Success() {
		// Hazırlık
		String username = "test.trainee";
		TraineeResponse expectedResponse = new TraineeResponse();
		expectedResponse.setUsername(username);

		when(traineeService.findTraineeByUsername(username)).thenReturn(expectedResponse);

		// Çağrı
		TraineeResponse actualResponse = gymCRMFacade.findTraineeByUsername(username);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals(username, actualResponse.getUsername());
		verify(traineeService, times(1)).findTraineeByUsername(username);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void findTraineeByUsername_Failure_NotFound() {
		// Hazırlık
		String username = "nonexistent.user";
		when(traineeService.findTraineeByUsername(username)).thenThrow(new RuntimeException("Trainee not found"));

		// Çağrı ve Doğrulama
		assertThrows(RuntimeException.class, () -> gymCRMFacade.findTraineeByUsername(username));
		verify(traineeService, times(1)).findTraineeByUsername(username);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void getAllTrainees_Success() {
		// Hazırlık
		List<TraineeResponse> expectedList = Arrays.asList(new TraineeResponse(), new TraineeResponse());
		when(traineeService.getAllTrainees()).thenReturn(expectedList);

		// Çağrı
		List<TraineeResponse> actualList = gymCRMFacade.getAllTrainees();

		// Doğrulama
		assertNotNull(actualList);
		assertEquals(2, actualList.size());
		verify(traineeService, times(1)).getAllTrainees();
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void updateTrainee_Success() {
		
		TraineeUpdateRequest request = new TraineeUpdateRequest("test.trainee", "Updated", "User",
				LocalDate.of(1990, 1, 1), "New Address");
		TraineeResponse expectedResponse = new TraineeResponse();
		expectedResponse.setUsername("test.trainee");
		expectedResponse.setFirstName("Updated");
		expectedResponse.setActive(true); // TraineeResponse'ta isActive alanı olmaya devam edebilir

		when(traineeService.updateTrainee(request)).thenReturn(expectedResponse);

		// Çağrı
		TraineeResponse actualResponse = gymCRMFacade.updateTrainee(request);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals("Updated", actualResponse.getFirstName());
		assertEquals("test.trainee", actualResponse.getUsername());
		// TraineeResponse'taki isActive kontrolü, servisin değeri kendisinin atadığını
		// varsayar.
		assertEquals(true, actualResponse.isActive());
		verify(traineeService, times(1)).updateTrainee(request);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void updateTraineeTrainers_Success() {
		// Hazırlık
		TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest("trainee.user",
				List.of("trainer.one", "trainer.two"));
		TraineeResponse expectedResponse = new TraineeResponse();
		expectedResponse.setUsername("trainee.user");

		when(traineeService.updateTraineeTrainersList(request)).thenReturn(expectedResponse);

		// Çağrı
		TraineeResponse actualResponse = gymCRMFacade.updateTraineeTrainers(request);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals("trainee.user", actualResponse.getUsername());
		verify(traineeService, times(1)).updateTraineeTrainersList(request);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void updateTraineeTrainers_Failure_TraineeNotFound() {
		// Hazırlık
		TraineeUpdateTrainersRequest request = new TraineeUpdateTrainersRequest("nonexistent.trainee",
				List.of("trainer.one"));
		when(traineeService.updateTraineeTrainersList(request)).thenThrow(new RuntimeException("Trainee not found"));

		// Çağrı ve Doğrulama
		assertThrows(RuntimeException.class, () -> gymCRMFacade.updateTraineeTrainers(request));
		verify(traineeService, times(1)).updateTraineeTrainersList(request);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void activateDeactivateTrainee_Success() {
		// Hazırlık
		UserActivationRequest request = new UserActivationRequest("test.trainee", true);
		doNothing().when(traineeService).activateDeactivateTrainee(request);

		// Çağrı
		gymCRMFacade.activateDeactivateTrainee(request);

		// Doğrulama
		verify(traineeService, times(1)).activateDeactivateTrainee(request);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void deleteTraineeById_Success() {
		// Hazırlık
		Long id = 1L;
		doNothing().when(traineeService).deleteTraineeById(id);

		// Çağrı
		gymCRMFacade.deleteTraineeById(id);

		// Doğrulama
		verify(traineeService, times(1)).deleteTraineeById(id);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void deleteTraineeByUsername_Success() {
		// Hazırlık
		String username = "test.trainee";
		doNothing().when(traineeService).deleteTraineeByUsername(username);

		// Çağrı
		gymCRMFacade.deleteTraineeByUsername(username);

		// Doğrulama
		verify(traineeService, times(1)).deleteTraineeByUsername(username);
		verifyNoMoreInteractions(traineeService);
		verifyNoInteractions(trainerService, trainingService);
	}

	@Test
	void getTraineeTrainingsList_Success() {
		// Hazırlık
		TraineeTrainingListRequest request = new TraineeTrainingListRequest("trainee.user", null, null, null, null);
		List<TrainingResponse> expectedList = Collections.singletonList(new TrainingResponse());
		when(trainingService.getTraineeTrainingsList(request)).thenReturn(expectedList);

		// Çağrı
		List<TrainingResponse> actualList = gymCRMFacade.getTraineeTrainingsList(request);

		// Doğrulama
		assertNotNull(actualList);
		assertFalse(actualList.isEmpty());
		verify(trainingService, times(1)).getTraineeTrainingsList(request);
		verifyNoMoreInteractions(trainingService);
		verifyNoInteractions(traineeService, trainerService);
	}

	// --- Trainer Facade Methods Test ---

	@Test
	void createTrainer_Success() {
		TrainerCreateRequest request = new TrainerCreateRequest("Jane", "Smith", true, "Yoga");
		TrainerResponse expectedResponse = new TrainerResponse();
		expectedResponse.setFirstName("Jane");
		expectedResponse.setLastName("Smith");
		expectedResponse.setUsername("Jane.Smith");
		expectedResponse.setActive(true);
		expectedResponse.setSpecializationName("Yoga");

		when(trainerService.createTrainer(request)).thenReturn(expectedResponse);

		TrainerResponse actualResponse = gymCRMFacade.createTrainer(request);

		assertNotNull(actualResponse);
		assertEquals("Jane.Smith", actualResponse.getUsername());
		assertEquals(true, actualResponse.isActive());
		verify(trainerService, times(1)).createTrainer(request);
		verifyNoMoreInteractions(trainerService);
		verifyNoInteractions(traineeService, trainingService);
	}

	@Test
	void findTrainerById_Success() {
		// Hazırlık
		Long id = 1L;
		TrainerResponse expectedResponse = new TrainerResponse();
		expectedResponse.setUsername("test.trainer");

		when(trainerService.findTrainerById(id)).thenReturn(expectedResponse);

		// Çağrı
		TrainerResponse actualResponse = gymCRMFacade.findTrainerById(id);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals("test.trainer", actualResponse.getUsername());
		verify(trainerService, times(1)).findTrainerById(id);
		verifyNoMoreInteractions(trainerService);
		verifyNoInteractions(traineeService, trainingService);
	}

	@Test
	void findTrainerByUsername_Success() {
		// Hazırlık
		String username = "test.trainer";
		TrainerResponse expectedResponse = new TrainerResponse();
		expectedResponse.setUsername(username);

		when(trainerService.findTrainerByUsername(username)).thenReturn(expectedResponse);

		// Çağrı
		TrainerResponse actualResponse = gymCRMFacade.findTrainerByUsername(username);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals(username, actualResponse.getUsername());
		verify(trainerService, times(1)).findTrainerByUsername(username);
		verifyNoMoreInteractions(trainerService);
		verifyNoInteractions(traineeService, trainingService);
	}

	@Test
	void getAllTrainers_Success() {
		// Hazırlık
		List<TrainerResponse> expectedList = Arrays.asList(new TrainerResponse(), new TrainerResponse());
		when(trainerService.getAllTrainers()).thenReturn(expectedList);

		// Çağrı
		List<TrainerResponse> actualList = gymCRMFacade.getAllTrainers();

		// Doğrulama
		assertNotNull(actualList);
		assertEquals(2, actualList.size());
		verify(trainerService, times(1)).getAllTrainers();
		verifyNoMoreInteractions(trainerService);
		verifyNoInteractions(traineeService, trainingService);
	}

	@Test
	void getUnassignedTrainersForTrainee_Success() {
		// Hazırlık
		String traineeUsername = "test.trainee";
		List<TrainerResponse> expectedList = Collections.singletonList(new TrainerResponse());
		when(trainerService.getUnassignedTrainersForTrainee(traineeUsername)).thenReturn(expectedList);

		// Çağrı
		List<TrainerResponse> actualList = gymCRMFacade.getUnassignedTrainersForTrainee(traineeUsername);

		// Doğrulama
		assertNotNull(actualList);
		assertFalse(actualList.isEmpty());
		verify(trainerService, times(1)).getUnassignedTrainersForTrainee(traineeUsername);
		verifyNoMoreInteractions(trainerService);
		verifyNoInteractions(traineeService, trainingService);
	}

	@Test
	void updateTrainer_Success() {
		TrainerUpdateRequest request = new TrainerUpdateRequest("test.trainer", "Updated", "Name", "Cardio");
		TrainerResponse expectedResponse = new TrainerResponse();
		expectedResponse.setUsername("test.trainer");
		expectedResponse.setFirstName("Updated");
		expectedResponse.setSpecializationName("Cardio");
		expectedResponse.setActive(true); // TrainerResponse'ta isActive alanı olmaya devam edebilir

		when(trainerService.updateTrainer(request)).thenReturn(expectedResponse);

		// Çağrı
		TrainerResponse actualResponse = gymCRMFacade.updateTrainer(request);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals("Updated", actualResponse.getFirstName());
		assertEquals("test.trainer", actualResponse.getUsername());
		assertEquals("Cardio", actualResponse.getSpecializationName());
		assertEquals(true, actualResponse.isActive()); // isActive kontrolü
		verify(trainerService, times(1)).updateTrainer(request);
		verifyNoMoreInteractions(trainerService);
		verifyNoInteractions(traineeService, trainingService);
	}

	@Test
	void activateDeactivateTrainer_Success() {
		// Hazırlık
		UserActivationRequest request = new UserActivationRequest("test.trainer", false);
		doNothing().when(trainerService).activateDeactivateTrainer(request);

		// Çağrı
		gymCRMFacade.activateDeactivateTrainer(request);

		// Doğrulama
		verify(trainerService, times(1)).activateDeactivateTrainer(request);
		verifyNoMoreInteractions(trainerService);
		verifyNoInteractions(traineeService, trainingService);
	}

	@Test
	void deleteTrainerById_Success() {
		// Hazırlık
		Long id = 1L;
		doNothing().when(trainerService).deleteTrainerById(id);

		// Çağrı
		gymCRMFacade.deleteTrainerById(id);

		// Doğrulama
		verify(trainerService, times(1)).deleteTrainerById(id);
		verifyNoMoreInteractions(trainerService);
		verifyNoInteractions(traineeService, trainingService);
	}

	@Test
	void deleteTrainerByUsername_Success() {
		// Hazırlık
		String username = "test.trainer";
		doNothing().when(trainerService).deleteTrainerByUsername(username);

		// Çağrı
		gymCRMFacade.deleteTrainerByUsername(username);

		// Doğrulama
		verify(trainerService, times(1)).deleteTrainerByUsername(username);
		verifyNoMoreInteractions(trainerService);
		verifyNoInteractions(traineeService, trainingService);
	}

	@Test
	void getTrainerTrainingsList_Success() {
		// Hazırlık
		TrainerTrainingListRequest request = new TrainerTrainingListRequest("trainer.user", null, null, null, null);
		List<TrainingResponse> expectedList = Collections.singletonList(new TrainingResponse());
		when(trainingService.getTrainerTrainingsList(request)).thenReturn(expectedList);

		// Çağrı
		List<TrainingResponse> actualList = gymCRMFacade.getTrainerTrainingsList(request);

		// Doğrulama
		assertNotNull(actualList);
		assertFalse(actualList.isEmpty());
		verify(trainingService, times(1)).getTrainerTrainingsList(request);
		verifyNoMoreInteractions(trainingService);
		verifyNoInteractions(traineeService, trainerService);
	}

	// --- Training Facade Methods Test ---

	@Test
	void createTraining_Success() {
		// Hazırlık
		TrainingCreateRequest request = new TrainingCreateRequest("Morning Yoga", LocalDate.now(), 60, "trainer.user",
				"trainee.user", "Yoga");
		TrainingResponse expectedResponse = new TrainingResponse();
		expectedResponse.setTrainingName("Morning Yoga");

		when(trainingService.createTraining(request)).thenReturn(expectedResponse);

		// Çağrı
		TrainingResponse actualResponse = gymCRMFacade.createTraining(request);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals("Morning Yoga", actualResponse.getTrainingName());
		verify(trainingService, times(1)).createTraining(request);
		verifyNoMoreInteractions(trainingService);
		verifyNoInteractions(traineeService, trainerService);
	}

	@Test
	void getTrainingById_Success() {
		// Hazırlık
		Long id = 1L;
		TrainingResponse expectedResponse = new TrainingResponse();
		expectedResponse.setTrainingName("Test Training");

		when(trainingService.getTrainingById(id)).thenReturn(expectedResponse);

		// Çağrı
		TrainingResponse actualResponse = gymCRMFacade.getTrainingById(id);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals("Test Training", actualResponse.getTrainingName());
		verify(trainingService, times(1)).getTrainingById(id);
		verifyNoMoreInteractions(trainingService);
		verifyNoInteractions(traineeService, trainerService);
	}

	@Test
	void getTrainingById_Failure_NotFound() {
		// Hazırlık
		Long id = 99L;
		when(trainingService.getTrainingById(id)).thenThrow(new RuntimeException("Training not found"));

		// Çağrı ve Doğrulama
		assertThrows(RuntimeException.class, () -> gymCRMFacade.getTrainingById(id));
		verify(trainingService, times(1)).getTrainingById(id);
		verifyNoMoreInteractions(trainingService);
		verifyNoInteractions(traineeService, trainerService);
	}

	@Test
	void getAllTrainings_Success() {
		// Hazırlık
		List<TrainingResponse> expectedList = Arrays.asList(new TrainingResponse(), new TrainingResponse());
		when(trainingService.getAllTrainings()).thenReturn(expectedList);

		// Çağrı
		List<TrainingResponse> actualList = gymCRMFacade.getAllTrainings();

		// Doğrulama
		assertNotNull(actualList);
		assertEquals(2, actualList.size());
		verify(trainingService, times(1)).getAllTrainings();
		verifyNoMoreInteractions(trainingService);
		verifyNoInteractions(traineeService, trainerService);
	}

	@Test
	void updateTraining_Success() {
		// Hazırlık
		TrainingUpdateRequest request = new TrainingUpdateRequest(1L, "Updated Training", LocalDate.now(), 75,
				"trainer.user", "trainee.user", "Cardio");
		TrainingResponse expectedResponse = new TrainingResponse();
		expectedResponse.setTrainingName("Updated Training");

		when(trainingService.updateTraining(request)).thenReturn(expectedResponse);

		// Çağrı
		TrainingResponse actualResponse = gymCRMFacade.updateTraining(request);

		// Doğrulama
		assertNotNull(actualResponse);
		assertEquals("Updated Training", actualResponse.getTrainingName());
		verify(trainingService, times(1)).updateTraining(request);
		verifyNoMoreInteractions(trainingService);
		verifyNoInteractions(traineeService, trainerService);
	}

	@Test
	void deleteTrainingById_Success() {
		// Hazırlık
		Long id = 1L;
		doNothing().when(trainingService).deleteTrainingById(id);

		// Çağrı
		gymCRMFacade.deleteTrainingById(id);

		// Doğrulama
		verify(trainingService, times(1)).deleteTrainingById(id);
		verifyNoMoreInteractions(trainingService);
		verifyNoInteractions(traineeService, trainerService);
	}

	@Test
	void deleteTrainingById_Failure_NotFound() {
		// Hazırlık
		Long id = 99L;
		doThrow(new RuntimeException("Training not found")).when(trainingService).deleteTrainingById(id);

		// Çağrı ve Doğrulama
		assertThrows(RuntimeException.class, () -> gymCRMFacade.deleteTrainingById(id));
		verify(trainingService, times(1)).deleteTrainingById(id);
		verifyNoMoreInteractions(trainingService);
		verifyNoInteractions(traineeService, trainerService);
	}
}