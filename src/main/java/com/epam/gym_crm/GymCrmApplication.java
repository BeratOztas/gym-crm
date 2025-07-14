package com.epam.gym_crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.epam.gym_crm.config.AppConfig;
import com.epam.gym_crm.facade.GymCRMFacade;

public class GymCrmApplication {

	private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		GymCRMFacade facade = context.getBean(GymCRMFacade.class);

		logger.info("--- Gym CRM Application Started Successfully ---");
		 
	}

}
