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
			logger.info("--- Gym CRM Uygulaması Başlatıldı ---");

			EntityManagerFactory emf = context.getBean(EntityManagerFactory.class);
			
			logger.info("Veritabanı bağlantısı başarılı: {}", emf.isOpen());

		} finally {
			context.close();
			logger.info("--- Gym CRM Uygulaması Kapatıldı ---");
		}
	}

}