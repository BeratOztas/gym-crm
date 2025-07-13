package com.epam.gym_crm.service;

import java.util.List;

import com.epam.gym_crm.model.Trainer;

public interface ITrainerService {

	Trainer findTrainerById(Long id);

	Trainer findTrainerByUsername(String username);

	List<Trainer> getAllTrainers();

	Trainer create(Trainer trainer);

	Trainer updateTrainer(Trainer trainer);
	
	boolean deleteTrainer(Long id);
}
