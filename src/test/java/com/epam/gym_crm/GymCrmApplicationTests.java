package com.epam.gym_crm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.epam.gym_crm.config.AppConfig;

@ExtendWith(SpringExtension.class) 
@ContextConfiguration(classes = {AppConfig.class})
class GymCrmApplicationTests {

	@Test
	void contextLoads() {
	}

}
