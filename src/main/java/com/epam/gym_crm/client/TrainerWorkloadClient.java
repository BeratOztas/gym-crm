package com.epam.gym_crm.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.epam.gym_crm.client.model.TrainerWorkloadRequest;

@FeignClient(name = "trainer-hours-service" , fallback = TrainerWorkloadClientFallback.class)
public interface TrainerWorkloadClient {
	
	@PostMapping("/api/v1/trainer-workload")
    void updateTrainerWorkload(
            @RequestBody TrainerWorkloadRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    );
}
