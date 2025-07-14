package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.epam.gym_crm.dao.ITraineeDAO;
import com.epam.gym_crm.dao.ITrainerDAO;
import com.epam.gym_crm.dao.ITrainingDAO;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.Training;
import com.epam.gym_crm.model.TrainingType;
import com.epam.gym_crm.service.init.IdGenerator;
import com.epam.gym_crm.utils.EntityType;

public class TrainingServiceImplTest {

	@Mock
	private ITrainingDAO trainingDAO;

	@Mock
	private ITraineeDAO traineeDAO;

	@Mock
	private ITrainerDAO trainerDAO;

	@Mock
	private IdGenerator idGenerator;

	@InjectMocks
	private TrainingServiceImpl trainingService;

	private Trainee dummyTrainee;
	private Trainer dummyTrainer;
	private Training dummyTraining;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		dummyTrainee = new Trainee(1L, "Ali", "Can", "ali.can", "pass", true, LocalDate.of(1995, 5, 20), 
				"Ankara, Türkiye"
		);
		dummyTrainer = new Trainer(2L, "Ayse", "Yilmaz", "ayse.yilmaz", "pass", true, TrainingType.YOGA);
		dummyTraining = new Training(10L, "Morning Yoga", TrainingType.YOGA, LocalDate.of(2025, 1, 1), 60, dummyTrainee,
				dummyTrainer);
	}

	@Test
	public void testFindById_validId_success() {
		when(trainingDAO.findById(10L)).thenReturn(Optional.of(dummyTraining));
		Training found = trainingService.findById(10L);
		assertEquals("Morning Yoga", found.getTrainingName());
	}

	@Test
	public void testFindById_invalidId_throwsException() {
		assertThrows(BaseException.class, () -> trainingService.findById(-1L));
	}

	@Test
	public void testGetAllTrainings_returnsList() {
		when(trainingDAO.findAll()).thenReturn(List.of(dummyTraining));
		List<Training> result = trainingService.getAllTrainings();
		assertEquals(1, result.size());
	}

	@Test
	public void testCreate_validTraining_success() {
		when(traineeDAO.findById(1L)).thenReturn(Optional.of(dummyTrainee));
		when(trainerDAO.findById(2L)).thenReturn(Optional.of(dummyTrainer));
		when(idGenerator.getNextId(EntityType.TRAINING)).thenReturn(10L);
		when(trainingDAO.create(any())).thenReturn(dummyTraining);

		Training result = trainingService.create(dummyTraining);
		assertEquals(10L, result.getId());
		assertEquals("Morning Yoga", result.getTrainingName());
	}

	@Test
	public void testCreate_nullTraining_throwsException() {
		assertThrows(BaseException.class, () -> trainingService.create(null));
	}

	@Test
	public void testUpdate_validTraining_success() {
		when(trainingDAO.findById(10L)).thenReturn(Optional.of(dummyTraining));
		when(traineeDAO.findById(1L)).thenReturn(Optional.of(dummyTrainee));
		when(trainerDAO.findById(2L)).thenReturn(Optional.of(dummyTrainer));
		when(trainingDAO.update(any())).thenReturn(dummyTraining);

		Training result = trainingService.update(dummyTraining);
		assertEquals("Morning Yoga", result.getTrainingName());
	}

	@Test
	public void testDelete_validId_success() {
		when(trainingDAO.delete(10L)).thenReturn(true);
		assertTrue(trainingService.delete(10L));
	}

	@Test
	public void testFindByTrainingName_success() {
		when(trainingDAO.findByTrainingName("Yoga")).thenReturn(List.of(dummyTraining));
		List<Training> results = trainingService.findByTrainingName("Yoga");
		assertEquals(1, results.size());
	}

	@Test
	public void testFindByTrainingDate_success() {
		when(trainingDAO.findByTrainingDate(LocalDate.of(2025, 1, 1))).thenReturn(List.of(dummyTraining));
		List<Training> results = trainingService.findByTrainingDate(LocalDate.of(2025, 1, 1));
		assertEquals(1, results.size());
	}

	@Test
	public void testFindByTrainingType_success() {
		when(trainingDAO.findByTrainingType(TrainingType.YOGA)).thenReturn(List.of(dummyTraining));
		List<Training> results = trainingService.findByTrainingType(TrainingType.YOGA);
		assertEquals(1, results.size());
	}

	@Test
	public void testFindByTraineeId_success() {
		when(trainingDAO.findByTraineeId(1L)).thenReturn(List.of(dummyTraining));
		List<Training> results = trainingService.findByTraineeId(1L);
		assertEquals(1, results.size());
	}

	@Test
	public void testFindByTrainerId_success() {
		when(trainingDAO.findByTrainerId(2L)).thenReturn(List.of(dummyTraining));
		List<Training> results = trainingService.findByTrainerId(2L);
		assertEquals(1, results.size());
	}
}
