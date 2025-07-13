package com.epam.gym_crm.facade;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.service.ITrainerService;

@Component
public class GymCRMFacade {
	private static final Logger logger = LoggerFactory.getLogger(GymCRMFacade.class);

	private final ITrainerService trainerService;

	public GymCRMFacade(ITrainerService trainerService) {
		this.trainerService = trainerService;
		logger.info("GymCRMFacade initialized.");
	}
	
	public Trainer createTrainer(Trainer trainer) {
        logger.info("Facade: Creating trainer...");
        return trainerService.create(trainer);
    }

    public Trainer getTrainerById(Long id) {
        logger.info("Facade: Fetching trainer by ID={}", id);
        return trainerService.findTrainerById(id);
    }
    
    public Trainer getTrainerByUsername(String username) {
    	logger.info("Facade: Fetching trainer by Username={}",username);
    	return trainerService.findTrainerByUsername(username);
    }

    public List<Trainer> getAllTrainers() {
        logger.info("Facade: Fetching all trainers");
        return trainerService.getAllTrainers();
    }

    public boolean deleteTrainer(Long id) {
        logger.info("Facade: Deleting trainer with ID={}", id);
        return trainerService.deleteTrainer(id);
    }
}
