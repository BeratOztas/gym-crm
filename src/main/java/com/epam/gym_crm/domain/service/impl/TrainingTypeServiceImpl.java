package com.epam.gym_crm.domain.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epam.gym_crm.auth.AuthManager;
import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.TrainingTypeRepository;
import com.epam.gym_crm.domain.service.ITrainingTypeService;

@Service
public class TrainingTypeServiceImpl implements ITrainingTypeService {

	private static final Logger logger = LoggerFactory.getLogger(TrainingTypeServiceImpl.class);

	private TrainingTypeRepository trainingTypeRepository;
	private AuthManager authManager;

	public TrainingTypeServiceImpl(TrainingTypeRepository trainingTypeRepository, AuthManager authManager) {
		this.trainingTypeRepository = trainingTypeRepository;
		this.authManager = authManager;
	}

	@Override
	@Transactional(readOnly = true)	
	public List<TrainingType> getTrainingTypes() {
		logger.info("Get TrainingType-Service Called."); 
		User currentUser = authManager.getCurrentUser();
		logger.info("User '{}' attempting to retrieve all training types.", currentUser.getUsername());
		List<TrainingType> trainingTypes = trainingTypeRepository.findAll();
		logger.info("User '{}' successfully retrieved {} training types.", currentUser.getUsername(),
				trainingTypes.size());
		return trainingTypes;
	}

	
}
