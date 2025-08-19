package com.epam.gym_crm.domain.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.epam.gym_crm.db.entity.TrainingType;
import com.epam.gym_crm.db.repository.TrainingTypeRepository;
import com.epam.gym_crm.domain.service.ITrainingTypeService;

@Service
public class TrainingTypeServiceImpl implements ITrainingTypeService {

	private static final Logger logger = LoggerFactory.getLogger(TrainingTypeServiceImpl.class);

	private TrainingTypeRepository trainingTypeRepository;
	private AuthenticationInfoService authenticationInfoService;

	
	public TrainingTypeServiceImpl(TrainingTypeRepository trainingTypeRepository,AuthenticationInfoService authenticationInfoService) {
		this.trainingTypeRepository = trainingTypeRepository;
		this.authenticationInfoService=authenticationInfoService;
	}

	@Override
	@Transactional(readOnly = true)	
	public List<TrainingType> getTrainingTypes() {
		logger.info("Get TrainingType-Service Called."); 
		String currentUsername = authenticationInfoService.getCurrentUsername();
		logger.info("User '{}' attempting to retrieve all training types.", currentUsername);
		List<TrainingType> trainingTypes = trainingTypeRepository.findAll();
		logger.info("User '{}' successfully retrieved {} training types.", currentUsername,
				trainingTypes.size());
		return trainingTypes;
	}

	
}
