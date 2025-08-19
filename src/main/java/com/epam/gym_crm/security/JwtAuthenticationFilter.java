package com.epam.gym_crm.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.epam.gym_crm.domain.service.impl.JwtTokenBlacklistService;
import com.epam.gym_crm.domain.service.impl.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	private final UserDetailsServiceImpl userDetailsService;

	private final JwtTokenBlacklistService jwtTokenBlacklistService;

	private final JwtTokenExtractor jwtTokenExtractor;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsServiceImpl userDetailsService,
			JwtTokenBlacklistService tokenBlacklistService, JwtTokenExtractor jwtTokenExtractor) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.userDetailsService = userDetailsService;
		this.jwtTokenBlacklistService = tokenBlacklistService;
		this.jwtTokenExtractor = jwtTokenExtractor;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String jwtToken = jwtTokenExtractor.extractJwtFromRequest(request);

		if (!StringUtils.hasText(jwtToken)) {
			filterChain.doFilter(request, response);
			return;
		}

		if (jwtTokenBlacklistService.isBlacklisted(jwtToken)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated by logout.");
			return;
		}

		String username = jwtTokenProvider.getUsernameFromToken(jwtToken);

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			UserDetails userDetails = userDetailsService.loadUserByUsername(username);

			// Validate token and account status
			if (jwtTokenProvider.validateToken(jwtToken) && userDetails.isEnabled()) {
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null,
						userDetails.getAuthorities());

				auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}

		filterChain.doFilter(request, response);

	}

}
