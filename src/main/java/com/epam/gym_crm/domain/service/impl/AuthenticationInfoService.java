package com.epam.gym_crm.domain.service.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.ErrorMessage;
import com.epam.gym_crm.domain.exception.MessageType;

@Service
public class AuthenticationInfoService {

	public String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new BaseException(new ErrorMessage(MessageType.UNAUTHORIZED, "User is not authenticated."));
		}
		return authentication.getName();
	}
}
