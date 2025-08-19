package com.epam.gym_crm.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.epam.gym_crm.config.JwtConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

	@Mock
	private JwtConfig jwtConfig;

	@InjectMocks
	private JwtTokenProvider jwtTokenProvider;

	private JwtUserDetails testUserDetails;
	private final String FAKE_SECRET = "this-is-a-very-long-and-secure-fake-secret-key-for-testing-purposes";
	private final long FAKE_EXPIRATION_MS = 3600000; // 1 saat

	@BeforeEach
	void setUp() {
		 lenient().when(jwtConfig.getSecret()).thenReturn(FAKE_SECRET);
		 lenient().when(jwtConfig.getExpirationMs()).thenReturn(FAKE_EXPIRATION_MS);

		testUserDetails = new JwtUserDetails(1L, "test.user", "password", true, true, Collections.emptyList());
	}

	// --- Token Generation and Parsing Tests ---

	@Test
	void shouldGenerateValidJwtToken() {
		// Act
		String token = jwtTokenProvider.generateJwtToken(testUserDetails);

		// Assert
		assertNotNull(token);
		assertFalse(token.isEmpty());

		Claims claims = Jwts.parserBuilder().setSigningKey(jwtTokenProvider.getKey()).build().parseClaimsJws(token)
				.getBody();

		assertEquals("1", claims.getSubject());
		assertEquals("test.user", claims.get("username", String.class));
	}

	@Test
	void shouldExtractUsernameFromToken() {

		String token = jwtTokenProvider.generateJwtToken(testUserDetails);

		String username = jwtTokenProvider.getUsernameFromToken(token);

		assertEquals("test.user", username);
	}

	@Test
	void shouldExtractUserIdFromToken() {
		String token = jwtTokenProvider.generateJwtToken(testUserDetails);

		Long userId = jwtTokenProvider.getUserIdFromToken(token);

		assertEquals(1L, userId);
	}

	// --- Token Validation Tests ---

	@Test
	void shouldReturnTrueForValidToken() {
		String token = jwtTokenProvider.generateJwtToken(testUserDetails);

		boolean isValid = jwtTokenProvider.validateToken(token);

		assertTrue(isValid, "A freshly generated token should be valid.");
	}

	@Test
	void shouldReturnFalseForExpiredToken() {
		Date pastDate = new Date(System.currentTimeMillis() - 5000);
		String expiredToken = Jwts.builder().setSubject("1").setExpiration(pastDate).signWith(jwtTokenProvider.getKey())
				.compact();

		boolean isValid = jwtTokenProvider.validateToken(expiredToken);

		assertFalse(isValid, "An expired token should not be valid.");
	}

	@Test
	void shouldReturnFalseForTokenWithWrongSignature() {
		String token = jwtTokenProvider.generateJwtToken(testUserDetails);

		JwtConfig wrongConfig = new JwtConfig();
		wrongConfig.setSecret("this-is-a-completely-different-wrong-secret-key");
		JwtTokenProvider wrongTokenProvider = new JwtTokenProvider(wrongConfig);

		boolean isValid = wrongTokenProvider.validateToken(token);

		assertFalse(isValid, "A token validated with a wrong secret key should be invalid.");
	}

	@Test
	void shouldReturnFalseForMalformedToken() {
		String malformedToken = "this.is.not.a.jwt";

		boolean isValid = jwtTokenProvider.validateToken(malformedToken);

		assertFalse(isValid, "A malformed token string should be invalid.");
	}

	@Test
	void shouldReturnFalseForNullOrEmptyToken() {
		String nullToken = null;
		String emptyToken = "";

		assertFalse(jwtTokenProvider.validateToken(nullToken), "A null token should be invalid.");
		assertFalse(jwtTokenProvider.validateToken(emptyToken), "An empty token should be invalid.");
	}
}
