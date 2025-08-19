package com.epam.gym_crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtConfig {

	private String secret;
	private long expirationMs;
	private long blacklistRetentionHours;
}
