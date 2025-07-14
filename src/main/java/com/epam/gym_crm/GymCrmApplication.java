package com.epam.gym_crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.epam.gym_crm.facade.GymCRMFacade;

@SpringBootApplication
public class GymCrmApplication {

	private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(GymCrmApplication.class, args);
		GymCRMFacade facade = context.getBean(GymCRMFacade.class);

		logger.info("--- Gym CRM Application Started Successfully ---");
		 
	}

}
