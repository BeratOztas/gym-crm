package com.epam.gym_crm.auth;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;

@Aspect
@Component
public class LogoutAspect {

	private static final Logger logger = LoggerFactory.getLogger(LogoutAspect.class);

	private final AuthManager authManager;

	public LogoutAspect(AuthManager authManager) {
		this.authManager = authManager;
	}

	@Pointcut("com.epam.gym_crm.config.AopConfig.logoutPointcut()")
	public void protectedServiceMethods() {
		// Empty method for define
	}

	// Runs after the endpoint method ,if successfull logouts
	@AfterReturning("protectedServiceMethods()")
	public void performLogoutOnSuccess(JoinPoint joinPoint) {
		try {
			if (authManager.isAuthenticated()) {
				String username = authManager.getCurrentUser().getUsername();
				authManager.logout();
				logger.info("<<<<<<<<<< Auto-logout successful for method: {} by user: {} >>>>>>>>>>",
						joinPoint.getSignature().toShortString(), username);
			}
		} catch (Exception e) {
			logger.error("Error during auto-logout (after return)", e);
			throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Error during auto-logout."));
		}
	}
	// Runs while endpoint method if throws any error still logouts
	@AfterThrowing(pointcut = "protectedServiceMethods()", throwing = "ex")
	public void performLogoutOnError(JoinPoint joinPoint, Throwable ex) {
		try {
			if (authManager.isAuthenticated()) {
				String username = authManager.getCurrentUser().getUsername();
				authManager.logout();
				logger.warn("<<<<<<<<<< Auto-logout after exception in method: {} by user: {} >>>>>>>>>>",
						joinPoint.getSignature().toShortString(), username);
			}
		} catch (Exception e) {
			logger.error("Error during auto-logout (after throwing)", e);
			throw new BaseException(
					new ErrorMessage(MessageType.GENERAL_EXCEPTION, "Error during auto-logout after exception."));
		}
	}
}