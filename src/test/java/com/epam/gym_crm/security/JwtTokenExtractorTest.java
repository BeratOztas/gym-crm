package com.epam.gym_crm.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtTokenExtractorTest {

	@InjectMocks
	private JwtTokenExtractor jwtTokenExtractor;

	@Mock
	private HttpServletRequest request;

	@BeforeEach
	void setUp() {
	}

	@Test
	void shouldExtractTokenFromValidAuthorizationHeader() {
		String token = "valid.jwt.token";
		String bearerToken = "Bearer " + token;
		when(request.getHeader("Authorization")).thenReturn(bearerToken);

		String extractedToken = jwtTokenExtractor.extractJwtFromRequest(request);

		assertEquals(token, extractedToken, "Token should be extracted correctly.");
	}

	@Test
	void shouldReturnNullWhenAuthorizationHeaderIsMissing() {
		// Arrange
		when(request.getHeader("Authorization")).thenReturn(null);

		// Act
		String extractedToken = jwtTokenExtractor.extractJwtFromRequest(request);

		// Assert
		assertNull(extractedToken, "Extracted token should be null.");
	}

	@Test
	void shouldReturnNullWhenAuthorizationHeaderDoesNotStartWithBearer() {
		String malformedToken = "Token-without-bearer-prefix";
		when(request.getHeader("Authorization")).thenReturn(malformedToken);

		String extractedToken = jwtTokenExtractor.extractJwtFromRequest(request);

		assertNull(extractedToken, "Extracted token should be null.");
	}

	@Test
	void shouldReturnNullWhenAuthorizationHeaderIsBlank() {
		when(request.getHeader("Authorization")).thenReturn("");

		String extractedToken = jwtTokenExtractor.extractJwtFromRequest(request);

		assertNull(extractedToken, "Extracted token should be null.");
	}
}
