package com.epam.gym_crm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.epam.gym_crm.config.AppConfig;

import jakarta.persistence.EntityManagerFactory;

public class GymCrmApplication {

	private static final Logger logger = LoggerFactory.getLogger(GymCrmApplication.class);

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		try {
			logger.info("--- Gym CRM Application Started Successfully ---");
			
			EntityManagerFactory emf = context.getBean(EntityManagerFactory.class);

	        System.out.println("✅ Bağlantı başarılı: " + emf.isOpen());

	        emf.close();
		} finally {
			context.close();
		}
	}

}
