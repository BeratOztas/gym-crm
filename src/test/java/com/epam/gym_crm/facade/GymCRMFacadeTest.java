package com.epam.gym_crm.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.model.User;
import com.epam.gym_crm.service.ITraineeService;
import com.epam.gym_crm.service.ITrainerService;
import com.epam.gym_crm.service.ITrainingService;

class GymCRMFacadeTest {

	private ITrainerService trainerService;
	private ITraineeService traineeService;
	private ITrainingService trainingService;
	private GymCRMFacade facade;

	@BeforeEach
	void setUp() {
		trainerService = mock(ITrainerService.class);
		traineeService = mock(ITraineeService.class);
		trainingService = mock(ITrainingService.class);
		facade = new GymCRMFacade(trainerService, traineeService, trainingService);
	}

	// ========== Trainee Tests ==========

	@Test
	void testCreateTrainee_success() {
		Trainee trainee = new Trainee();
		when(traineeService.create(trainee)).thenReturn(trainee);
		assertEquals(trainee, facade.createTrainee(trainee));
	}

	@Test
	void testGetTraineeById_success() {
		Trainee trainee = new Trainee();
		when(traineeService.findTraineeById(1L)).thenReturn(trainee);
		assertEquals(trainee, facade.getTraineeById(1L));
	}

	@Test
	void testDeleteTrainee_failure() {
		when(traineeService.deleteTrainee(99L)).thenReturn(false);
		assertFalse(facade.deleteTrainee(99L));
	}

	// ========== Trainer Tests ==========

	@Test
	void testCreateTrainer_success() {
		Trainer trainer = new Trainer();
		when(trainerService.create(trainer)).thenReturn(trainer);
		assertEquals(trainer, facade.createTrainer(trainer));
	}

	@Test
	void testGetTrainerByUsername_notFound() {
		when(trainerService.findTrainerByUsername("unknown")).thenReturn(null);
		assertNull(facade.getTrainerByUsername("unknown"));
	}

	// ========== Training Tests ==========

	@Test
	void testCreateTraining_success() {
		Training training = new Training();
		when(trainingService.create(training)).thenReturn(training);
		assertEquals(training, facade.createTraining(training));
	}

	@Test
	void testGetTrainingsByDate_success() {
		LocalDate date = LocalDate.now();
		List<Training> expected = Arrays.asList(new Training());
		when(trainingService.findByTrainingDate(date)).thenReturn(expected);
		assertEquals(expected, facade.getTrainingsByTrainingDate(date));
	}

	@Test
	void testGetTrainingsByTrainerId_empty() {
		when(trainingService.findByTrainerId(123L)).thenReturn(Collections.emptyList());
		assertTrue(facade.getTrainingsByTrainerId(123L).isEmpty());
	}

	@Test
	void testDeleteTraining_success() {
		when(trainingService.delete(10L)).thenReturn(true);
		assertTrue(facade.deleteTraining(10L));
	}

	// --- Trainee Failure ---

	@Test
	void testCreateTrainee_serviceThrowsException() {
		Trainee inputTrainee = new Trainee();
		when(traineeService.create(any(Trainee.class))).thenThrow(
				new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Trainee verileri geçersiz.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.createTrainee(inputTrainee));
		assertTrue(exception.getMessage().contains("Trainee verileri geçersiz."));
		verify(traineeService, times(1)).create(any(Trainee.class));
	}

	@Test
	void testGetTraineeById_notFound() {
		Long traineeId = 99L;
		when(traineeService.findTraineeById(traineeId))
				.thenThrow(new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainee bulunamadı.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.getTraineeById(traineeId));
		assertTrue(exception.getMessage().contains("Trainee bulunamadı."));
		verify(traineeService, times(1)).findTraineeById(traineeId);
	}

	@Test
	void testUpdateTrainee_nullInput_shouldThrow() {
		when(traineeService.updateTrainee(ArgumentMatchers.isNull())).thenThrow(
				new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Trainee objesi boş olamaz.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.updateTrainee(null));
		assertTrue(exception.getMessage().contains("Trainee objesi boş olamaz."));
		verify(traineeService, times(1)).updateTrainee(ArgumentMatchers.isNull());
	}

	@Test
	void testUpdateTrainee_notFound() {
		Trainee inputTrainee = new Trainee();
		User inputUser = new User();
		inputUser.setId(99L);
		inputTrainee.setUser(inputUser);

		when(traineeService.updateTrainee(any(Trainee.class))).thenThrow(new BaseException(
				new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Güncellenecek Trainee bulunamadı.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.updateTrainee(inputTrainee));
		assertTrue(exception.getMessage().contains("Güncellenecek Trainee bulunamadı."));
		verify(traineeService, times(1)).updateTrainee(any(Trainee.class));
	}

	@Test
	void testDeleteTrainee_serviceThrowsException() {
		Long traineeId = 99L;
		when(traineeService.deleteTrainee(traineeId)).thenThrow(
				new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Trainee silinirken hata oluştu.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.deleteTrainee(traineeId));
		assertTrue(exception.getMessage().contains("Trainee silinirken hata oluştu."));
		verify(traineeService, times(1)).deleteTrainee(traineeId);
	}

	@Test
	void testGetTraineeProfileByUsername_notFound() {
		String username = "bilinmeyen.kullanici";
		when(traineeService.findTraineeByUsername(username)).thenThrow(new BaseException(
				new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Kullanıcı adına göre Trainee bulunamadı.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.getTraineeByUsername(username));
		assertTrue(exception.getMessage().contains("Kullanıcı adına göre Trainee bulunamadı."));
		verify(traineeService, times(1)).findTraineeByUsername(username);
	}

	// --- Trainer Failure ---
	@Test
	void testUpdateTrainer_nullInput_shouldThrow() {
		when(trainerService.updateTrainer(ArgumentMatchers.isNull())).thenThrow(
				new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainer must not be null")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.updateTrainer(null));
		assertTrue(exception.getMessage().contains("Trainer must not be null"));
		verify(trainerService, times(1)).updateTrainer(ArgumentMatchers.isNull());
	}

	@Test
	void testCreateTrainer_serviceThrowsException() {
		Trainer inputTrainer = new Trainer();
		when(trainerService.create(any(Trainer.class))).thenThrow(
				new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Trainer verileri geçersiz.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.createTrainer(inputTrainer));
		assertTrue(exception.getMessage().contains("Trainer verileri geçersiz."));
		verify(trainerService, times(1)).create(any(Trainer.class));
	}

	@Test
	void testGetTrainerById_notFound() {
		Long trainerId = 99L;
		when(trainerService.findTrainerById(trainerId))
				.thenThrow(new BaseException(new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Trainer bulunamadı.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.getTrainerById(trainerId));
		assertTrue(exception.getMessage().contains("Trainer bulunamadı."));
		verify(trainerService, times(1)).findTrainerById(trainerId);
	}

	@Test
	void testUpdateTrainer_notFound() {
		Trainer inputTrainer = new Trainer();
		User inputUser = new User();
		inputUser.setId(99L);
		inputTrainer.setUser(inputUser);

		when(trainerService.updateTrainer(any(Trainer.class))).thenThrow(new BaseException(
				new ErrorMessage(MessageType.RESOURCE_NOT_FOUND, "Güncellenecek Trainer bulunamadı.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.updateTrainer(inputTrainer));
		assertTrue(exception.getMessage().contains("Güncellenecek Trainer bulunamadı."));
		verify(trainerService, times(1)).updateTrainer(any(Trainer.class));
	}

	// --- Training Failure ---

	@Test
	void testCreateTraining_serviceThrowsException() {
		Training inputTraining = new Training();
		when(trainingService.create(any(Training.class))).thenThrow(
				new BaseException(new ErrorMessage(MessageType.INVALID_ARGUMENT, "Antrenman verileri geçersiz.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.createTraining(inputTraining));
		assertTrue(exception.getMessage().contains("Antrenman verileri geçersiz."));
		verify(trainingService, times(1)).create(any(Training.class));
	}

	@Test
	void testDeleteTraining_serviceThrowsException() {
		Long trainingId = 99L;
		when(trainingService.delete(trainingId)).thenThrow(new BaseException(
				new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Antrenman silinirken hata oluştu.")));

		BaseException exception = assertThrows(BaseException.class, () -> facade.deleteTraining(trainingId));
		assertTrue(exception.getMessage().contains("Antrenman silinirken hata oluştu."));
		verify(trainingService, times(1)).delete(trainingId);
	}
}
