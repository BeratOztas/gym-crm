package com.epam.gym_crm.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.epam.gym_crm.domain.service.impl.JwtTokenBlacklistService;
import com.epam.gym_crm.domain.service.impl.UserDetailsServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private UserDetailsServiceImpl userDetailsService;
	@Mock
	private JwtTokenBlacklistService jwtTokenBlacklistService;
	@Mock
	private JwtTokenExtractor jwtTokenExtractor;

	@InjectMocks
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private MockFilterChain filterChain;

	@BeforeEach
	void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		filterChain = new MockFilterChain();

		SecurityContextHolder.clearContext();
	}

	// --- Success Scenario ---

	@Test
	void shouldAuthenticateUserWhenTokenIsValidAndNotInBlacklist() throws ServletException, IOException {

		String validToken = "valid.jwt.token";
		String username = "test.user";
		UserDetails userDetails = new User(username, "password", Collections.emptyList());

		when(jwtTokenExtractor.extractJwtFromRequest(request)).thenReturn(validToken);
		when(jwtTokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);
		when(jwtTokenProvider.getUsernameFromToken(validToken)).thenReturn(username);
		when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
		when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication(),
				"Authentication should be set in SecurityContext.");
		assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName(),
				"Authenticated user should be 'test.user'.");
		// Filtrenin zincirde devam ettiğini doğrula
		assertNull(response.getErrorMessage(), "Response should not have an error message.");
		assertTrue(filterChain.getRequest() != null, "FilterChain's doFilter should have been called.");
	}

	// --- Failure Scenario ---

	@Test
	void shouldNotAuthenticateWhenTokenIsMissing() throws ServletException, IOException {
		
		when(jwtTokenExtractor.extractJwtFromRequest(request)).thenReturn(null);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication should NOT be set.");
		assertTrue(filterChain.getRequest() != null, "FilterChain should still continue for public endpoints.");
	}

	@Test
	void shouldBlockRequestWhenTokenIsBlacklisted() throws ServletException, IOException {
		
		String blacklistedToken = "blacklisted.jwt.token";
		when(jwtTokenExtractor.extractJwtFromRequest(request)).thenReturn(blacklistedToken);
		when(jwtTokenBlacklistService.isBlacklisted(blacklistedToken)).thenReturn(true);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus(),
				"Response status should be 401 Unauthorized.");
		assertTrue(response.getErrorMessage().contains("Token has been invalidated"),
				"Error message should indicate a blacklisted token.");
		assertNull(SecurityContextHolder.getContext().getAuthentication(),
				"Authentication should NOT be set for a blacklisted token.");
		//Filtre zinciri bu noktada kesilmeli.
		assertNull(filterChain.getRequest(), "FilterChain's doFilter should NOT have been called.");
	}

	@Test
	void shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
		// Arrange
		String invalidToken = "invalid.jwt.token";
		String username = "test.user";
		UserDetails userDetails = new User(username, "password", Collections.emptyList());

		when(jwtTokenExtractor.extractJwtFromRequest(request)).thenReturn(invalidToken);
		when(jwtTokenBlacklistService.isBlacklisted(invalidToken)).thenReturn(false);
		when(jwtTokenProvider.getUsernameFromToken(invalidToken)).thenReturn(username);
		when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
		when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication(),
				"Authentication should not be set for an invalid token.");
		assertTrue(filterChain.getRequest() != null, "FilterChain should continue even if token is invalid.");
	}

	@Test
	void shouldNotAuthenticateWhenUserIsDisabled() throws ServletException, IOException {
		String validToken = "valid.jwt.token";
		String username = "disabled.user";
		// isEnabled() metodu false dönecek bir UserDetails nesnesi oluşturuyoruz.
		UserDetails disabledUserDetails = new User(username, "password", false, true, true, true,
				Collections.emptyList());

		when(jwtTokenExtractor.extractJwtFromRequest(request)).thenReturn(validToken);
		when(jwtTokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);
		when(jwtTokenProvider.getUsernameFromToken(validToken)).thenReturn(username);
		when(userDetailsService.loadUserByUsername(username)).thenReturn(disabledUserDetails);
		when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication(),
				"Authentication should not be set for a disabled user.");
		assertTrue(filterChain.getRequest() != null, "FilterChain should continue.");
	}

	@Test
	void shouldNotAuthenticateIfUserAlreadyAuthenticatedInContext() throws ServletException, IOException {
		// Arrange
		String token = "valid.token";

		// SecurityContext'e önceden bir kimlik doğrulama bilgisi yerleştiriyoruz.
		Authentication existingAuth = new UsernamePasswordAuthenticationToken("already.logged.in", null);
		SecurityContextHolder.getContext().setAuthentication(existingAuth);

		when(jwtTokenExtractor.extractJwtFromRequest(request)).thenReturn(token);
		
		when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("new.user");

		// Act
		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		// userDetailsService'in hiç çağrılmaması gerekiyor çünkü context'te zaten bir
		// kullanıcı var.
		verify(userDetailsService, never()).loadUserByUsername(anyString());
		assertEquals("already.logged.in", SecurityContextHolder.getContext().getAuthentication().getName(),
				"Existing authentication should not be overridden.");
		assertTrue(filterChain.getRequest() != null, "FilterChain should continue.");
	}
}