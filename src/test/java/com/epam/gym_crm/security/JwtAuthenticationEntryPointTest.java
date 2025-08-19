package com.epam.gym_crm.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationEntryPointTest {

	@InjectMocks
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private AuthenticationException authException;

	private static final String ERROR_MESSAGE = "Unauthorized user access!";

	@BeforeEach
	void setUp() {
		when(authException.getMessage()).thenReturn(ERROR_MESSAGE);
	}

	@Test
	void shouldSendUnauthorizedErrorOnCommence() throws IOException {
		try {
			jwtAuthenticationEntryPoint.commence(request, response, authException);
		} catch (jakarta.servlet.ServletException e) {
		}

		verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, ERROR_MESSAGE);

		verifyNoMoreInteractions(response);
	}
}
