package com.epam.gym_crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.security.login")
@Getter
@Setter
public class LoginSecurityConfig {

	private int maxAttempts;
	private long lockoutDurationMinutes;
}
