package com.epam.gym_crm.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.epam.gym_crm.client.model.TrainerWorkloadRequest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name = "trainer-hours-service")
public interface TrainerWorkloadClient {
	
	Logger logger = LoggerFactory.getLogger(TrainerWorkloadClient.class);
	
	@PostMapping("/api/v1/trainer-workload")
	@CircuitBreaker(name = "trainerHoursService",fallbackMethod = "handleFallback")
    void updateTrainerWorkload(
            @RequestBody TrainerWorkloadRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    );

	default void handleFallback(TrainerWorkloadRequest request,String token, Throwable t) {
		    logger.warn("Circuit Breaker activated. Failed to update workload for trainer '{}'. Reason : {}",
		                request.trainerUsername(),t.getMessage());
	}
	
}
