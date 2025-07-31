package com.epam.gym_crm.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.epam.gym_crm.auth.AuthManager;
import com.epam.gym_crm.model.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionManagementFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(SessionManagementFilter.class);

	private final AuthManager authManager;

	public SessionManagementFilter(AuthManager authManager) {
		this.authManager = authManager;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			//1.HTTP Session'dan kullanıcı bilgisini al
			HttpSession session = request.getSession(false);
			if (session != null) {
				User user = (User) session.getAttribute("currentUser");
				if (user != null) {
					// 2. AuthManager'ın ThreadLocal'ını doldur
					authManager.login(user);
					logger.debug("User '{}' found in session and logged into AuthManager.", user.getUsername());
					session.invalidate(); // Sadece 1 kere izin ver. 
				}
			}

			// 3. İstek zincirini devam ettir
			filterChain.doFilter(request, response);
		} finally {
			// 4. Her istekten sonra ThreadLocal'ı temizle
			if (authManager.isAuthenticated()) {
				logger.debug("Auto-logout performed for current request.");
				authManager.logout();
			}
		}
	}
}