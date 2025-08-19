package com.epam.gym_crm.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtTokenExtractor {

	private static final String AUTH_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	public String extractJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTH_HEADER);

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(BEARER_PREFIX.length()); // 'Bearer ' remove string
		}

		return null;
	}
}
