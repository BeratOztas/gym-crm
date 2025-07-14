package com.epam.gym_crm.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.epam.gym_crm.dao.ITraineeDAO;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.User;
import com.epam.gym_crm.service.init.IdGenerator;
import com.epam.gym_crm.utils.EntityType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TraineeServiceImplTest {

    private TraineeServiceImpl traineeService;
    private ITraineeDAO traineeDAO;
    private IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        traineeDAO = mock(ITraineeDAO.class);
        idGenerator = mock(IdGenerator.class);

        traineeService = new TraineeServiceImpl();
        traineeService.setIdGenerator(idGenerator);
        ReflectionTestUtils.setField(traineeService, "traineeDAO", traineeDAO);
    }

    @Test
    void shouldCreateTraineeSuccessfully() {
        Trainee ali = new Trainee(null, "Ali", "Yılmaz", null, null, false,
                LocalDate.of(2000, 5, 15), "Ankara");

        when(idGenerator.getNextId(EntityType.TRAINEE)).thenReturn(1L);
        when(traineeDAO.findByUsername("Ali.Yılmaz")).thenReturn(Optional.empty());
        when(traineeDAO.create(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdTraineeUser = traineeService.create(ali).getUser();

        assertNotNull(createdTraineeUser.getId());
        assertNotNull(createdTraineeUser.getUsername());
        assertNotNull(createdTraineeUser.getPassword());
        assertTrue(createdTraineeUser.isActive());

        verify(traineeDAO).create(any(Trainee.class));
    }

    @Test
    void shouldFindTraineeById() {
        Trainee ayse = new Trainee(1L, "Ayşe", "Demir", "Ayşe.Demir", "şifre123", true,
                LocalDate.of(1998, 3, 10), "İstanbul");

        when(traineeDAO.findById(1L)).thenReturn(Optional.of(ayse));

        Trainee result = traineeService.findTraineeById(1L);
        assertEquals("Ayşe", result.getUser().getFirstName());
    }

    @Test
    void shouldThrowExceptionWhenTraineeNotFoundById() {
        when(traineeDAO.findById(999L)).thenReturn(Optional.empty());

        BaseException e = assertThrows(BaseException.class, () -> traineeService.findTraineeById(999L));
        assertTrue(e.getMessage().contains("Trainee not found"));
    }

    @Test
    void shouldFindTraineeByUsername() {
        Trainee mehmet = new Trainee(5L, "Mehmet", "Kaya", "Mehmet.Kaya", "şifre", true,
                LocalDate.of(1995, 1, 1), "Bursa");

        when(traineeDAO.findByUsername("Mehmet.Kaya")).thenReturn(Optional.of(mehmet));

        Trainee result = traineeService.findTraineeByUsername("Mehmet.Kaya");
        assertEquals("Mehmet", result.getUser().getFirstName());
    }

    @Test
    void shouldThrowExceptionWhenTraineeNotFoundByUsername() {
        when(traineeDAO.findByUsername("bilinmeyen")).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> traineeService.findTraineeByUsername("bilinmeyen"));
    }

    @Test
    void shouldUpdateTraineeSuccessfully() {
        Trainee updatedTrainee = new Trainee(1L, "Ali", "Güncel", null, null, false,
                LocalDate.of(1995, 1, 1), "İzmir");

        Trainee existing = new Trainee(1L, "Ali", "Eski", "Ali.Esli", "pass", true,
                LocalDate.of(1990, 1, 1), "Eskişehir");

        when(traineeDAO.findById(1L)).thenReturn(Optional.of(existing));
        when(traineeDAO.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee result = traineeService.updateTrainee(updatedTrainee);

        assertEquals("Güncel", result.getUser().getLastName());
        assertEquals("İzmir", result.getAddress());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingTrainee() {
        Trainee input = new Trainee(999L, "Ali", "Güncel", null, null, false,
                LocalDate.of(1995, 1, 1), "İzmir");

        when(traineeDAO.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> traineeService.updateTrainee(input));
    }

    @Test
    void shouldDeleteTraineeSuccessfully() {
        when(traineeDAO.delete(1L)).thenReturn(true);

        assertTrue(traineeService.deleteTrainee(1L));
        verify(traineeDAO).delete(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTrainee() {
        when(traineeDAO.delete(999L)).thenReturn(false);

        assertThrows(BaseException.class, () -> traineeService.deleteTrainee(999L));
    }
    
    @Test
    void shouldThrowExceptionWhenCreatingNullTrainee() {
        assertThrows(BaseException.class, () -> traineeService.create(null));
    }

    @Test
    void shouldGetAllTrainees() {
        Trainee t1 = new Trainee(1L, "Ali", "A", "Ali.A", "pass", true, LocalDate.of(1990, 1, 1), "Ankara");
        Trainee t2 = new Trainee(2L, "Ayşe", "B", "Ayşe.B", "pass", true, LocalDate.of(1992, 1, 1), "İstanbul");

        when(traineeDAO.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<Trainee> result = traineeService.getAllTrainees();
        assertEquals(2, result.size());
    }
}
