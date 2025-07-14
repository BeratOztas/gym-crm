package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.epam.gym_crm.dao.ITrainerDAO;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.MessageType;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.TrainingType;
import com.epam.gym_crm.service.init.IdGenerator;
import com.epam.gym_crm.utils.EntityType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TrainerServiceImplTest {

    private TrainerServiceImpl trainerService;
    private ITrainerDAO trainerDAO;
    private IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl();
        trainerDAO = mock(ITrainerDAO.class);
        idGenerator = mock(IdGenerator.class);

        ReflectionTestUtils.setField(trainerService, "trainerDAO", trainerDAO);
        trainerService.setIdGenerator(idGenerator);
    }

    @Test
    @DisplayName("Should create trainer successfully")
    void testCreateTrainer() {
        Trainer newTrainer = new Trainer(null, "Merve", "Yılmaz", null, null, true, TrainingType.YOGA);
        Trainer savedTrainer = new Trainer(1L, "Merve", "Yılmaz", "Merve.Yılmaz", "123456", true, TrainingType.YOGA);

        when(idGenerator.getNextId(EntityType.TRAINER)).thenReturn(1L);
        when(trainerDAO.findByUsername("Merve.Yılmaz")).thenReturn(Optional.empty());
        when(trainerDAO.create(any(Trainer.class))).thenReturn(savedTrainer);

        Trainer result = trainerService.create(newTrainer);

        assertEquals("Merve.Yılmaz", result.getUser().getUsername());
        assertEquals(1L, result.getUser().getId());
        assertEquals(TrainingType.YOGA, result.getSpecialization());
    }

    @Test
    @DisplayName("Should throw exception when creating null trainer")
    void testCreateTrainerWithNullInput() {
        assertThrows(BaseException.class, () -> trainerService.create(null));
    }

    @Test
    @DisplayName("Should find trainer by ID")
    void testFindTrainerById() {
        Trainer trainer = new Trainer(1L, "Mehmet", "Demir", "mehmet.demir", "pass", true, TrainingType.FITNESS);
        when(trainerDAO.findById(1L)).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.findTrainerById(1L);
        assertEquals("Mehmet", result.getUser().getFirstName());
    }

    @Test
    @DisplayName("Should throw exception when trainer ID is invalid")
    void testFindTrainerByInvalidId() {
        BaseException ex = assertThrows(BaseException.class, () -> trainerService.findTrainerById(-1L));
        String generalInvalidArgMessage = MessageType.INVALID_ARGUMENT.getMessage();
        assertTrue(ex.getMessage().startsWith(generalInvalidArgMessage + " : "));
        assertTrue(ex.getMessage().contains("ID must be a positive value"));
        assertTrue(ex.getMessage().contains("-1"));
    }

    @Test
    @DisplayName("Should throw exception when trainer not found by ID")
    void testFindTrainerByIdNotFound() {
        when(trainerDAO.findById(999L)).thenReturn(Optional.empty());

        BaseException e = assertThrows(BaseException.class, () -> trainerService.findTrainerById(999L));
        assertTrue(e.getMessage().contains("Trainer not found"));
    }

    @Test
    @DisplayName("Should update existing trainer")
    void testUpdateTrainer() {
        Trainer existing = new Trainer(1L, "Ali", "Kaya", "Ali.Kaya", "pass", true, TrainingType.FITNESS);
        Trainer update = new Trainer(1L, "AliUpdated", null, null, null, false, TrainingType.YOGA);

        when(trainerDAO.findById(1L)).thenReturn(Optional.of(existing));
        when(trainerDAO.update(any(Trainer.class))).thenReturn(existing);

        Trainer updated = trainerService.updateTrainer(update);

        assertEquals("AliUpdated", updated.getUser().getFirstName());
        assertEquals(TrainingType.YOGA, updated.getSpecialization());
        assertFalse(updated.getUser().isActive());
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing trainer")
    void testUpdateNonExistingTrainer() {
        Trainer update = new Trainer(999L, "No", "Body", null, null, false, TrainingType.YOGA);

        when(trainerDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> trainerService.updateTrainer(update));
    }

    @Test
    @DisplayName("Should delete trainer successfully")
    void testDeleteTrainer() {
        when(trainerDAO.delete(1L)).thenReturn(true);
        assertTrue(trainerService.deleteTrainer(1L));
    }

    @Test
    @DisplayName("Should throw exception when trainer deletion fails")
    void testDeleteNonExistingTrainer() {
        when(trainerDAO.delete(999L)).thenReturn(false);
        assertThrows(BaseException.class, () -> trainerService.deleteTrainer(999L));
    }

    @Test
    @DisplayName("Should get all trainers")
    void testGetAllTrainers() {
        List<Trainer> trainers = Arrays.asList(
                new Trainer(1L, "Asli", "Akgün", "asli.akgun", "1234", true, TrainingType.YOGA),
                new Trainer(2L, "Baris", "Demir", "baris.demir", "abcd", true, TrainingType.FITNESS)
        );

        when(trainerDAO.findAll()).thenReturn(trainers);
        List<Trainer> result = trainerService.getAllTrainers();

        assertEquals(2, result.size());
    }
}
