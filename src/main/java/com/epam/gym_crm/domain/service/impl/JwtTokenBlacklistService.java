package com.epam.gym_crm.domain.service.impl;

import com.epam.gym_crm.config.JwtConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class JwtTokenBlacklistService {

	private static final Logger logger = LoggerFactory.getLogger(JwtTokenBlacklistService.class);
	
	private final Cache<String, Boolean> tokenBlacklist;

	public JwtTokenBlacklistService(JwtConfig jwtConfig) {
		 long retentionHours = jwtConfig.getBlacklistRetentionHours();
		this.tokenBlacklist = CacheBuilder.newBuilder()
				.expireAfterWrite(retentionHours, TimeUnit.HOURS)

				.build();
		
		logger.info("JwtTokenBlacklistService initialized. Blacklisted tokens will be retained for {} hours.", retentionHours);
	}

	public void blacklistToken(String token) {
		if (token != null && !token.isEmpty()) {
			tokenBlacklist.put(token, true);
			
			String partialToken = token.length() > 10 ? token.substring(0, 10) + "..." : token;
            logger.info("Token starting with '{}' has been added to the blacklist.", partialToken);
		}
	}

	public boolean isBlacklisted(String token) {
        if (token == null) {
            return false;
        }
		return tokenBlacklist.getIfPresent(token) != null;
	}
}
