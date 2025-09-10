package com.epam.gym_crm.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.epam.gym_crm.client.model.TrainerWorkloadRequest;

@Component
public class TrainerWorkloadClientFallback implements TrainerWorkloadClient {

	private static final Logger logger = LoggerFactory.getLogger(TrainerWorkloadClientFallback.class);
	
	@Override
	public void updateTrainerWorkload(TrainerWorkloadRequest request, String token) {
		logger.warn("Circuit Breaker activated. Failed to update workload for trainer '{}'.", request.trainerUsername());
		
	}
}
