package com.epam.gym_crm.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.epam.trainingcommons.dto.TrainerWorkloadRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TrainingProducer {

	private static final Logger logger = LoggerFactory.getLogger(TrainingProducer.class);

	private final KafkaTemplate<String, TrainerWorkloadRequest> kafkaTemplate;
	
	@Value("${topic.trainer-workload}")
	private  String topicName;
	
	public void sendWorkloadUpdate(TrainerWorkloadRequest request) {
		logger.info("Sending message to Kafka topic '{}' for trainer '{}' with transactionId '{}'",
                topicName, request.trainerUsername(), request.transactionId());
		
		kafkaTemplate.send(topicName, request);
		
		logger.info("Message sent successfully.");
	}

}
