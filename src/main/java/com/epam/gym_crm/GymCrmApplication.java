package com.epam.gym_crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.epam.gym_crm.config.AppConfig;

public class GymCrmApplication {

	private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		try {
			logger.info("--- Gym CRM Application Started Successfully ---");
		} finally {
			context.close();
		}
	}

}
