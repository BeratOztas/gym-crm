package com.epam.gym_crm.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;

import com.epam.gym_crm.model.Trainee;
import com.epam.gym_crm.model.Trainer;
import com.epam.gym_crm.model.Training;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@ComponentScan(basePackages = "com.epam.gym_crm")
@PropertySource("classpath:application.properties")
@EnableAspectJAutoProxy
public class AppConfig {
	
	@Bean(name = "traineeStorageMap")
	public Map<Long, Trainee> traineeStorageMap(){
		return new ConcurrentHashMap<>();
	}

	@Bean(name = "trainerStorageMap")
	public Map<Long, Trainer> trainerStorageMap(){
		return new ConcurrentHashMap<>();
	}
	
	@Bean(name = "trainingStorageMap")
	public Map<Long, Training> trainingStorageMap(){
		return new ConcurrentHashMap<>();
	}
	
	@Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); 
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}
