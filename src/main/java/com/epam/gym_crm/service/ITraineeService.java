package com.epam.gym_crm.service;

import java.util.List;

import com.epam.gym_crm.model.Trainee;

public interface ITraineeService {

	Trainee findTraineeById(Long id);

	Trainee findTraineeByUsername(String username);

	List<Trainee> getAllTrainees();

	Trainee create(Trainee trainee);

	Trainee updateTrainee(Trainee trainee);

	boolean deleteTrainee(Long id);
}
