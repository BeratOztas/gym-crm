package com.epam.gym_crm.mq.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

	@Value("${topic.trainer-workload}")
	public  String trainerWorkloadTopicName;

	@Value("${topic.partitions:1}")
	private int partitions;

	@Value("${topic.replicas:1}")
	private int replicas;

	@Bean
	public NewTopic trainerWorkloadTopic() {
		return TopicBuilder.name(trainerWorkloadTopicName)
				.partitions(partitions)
				.replicas(replicas)
				.build();

	}
}
