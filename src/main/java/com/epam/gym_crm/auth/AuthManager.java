package com.epam.gym_crm.auth;

import org.springframework.stereotype.Component;

import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;

@Component
public class AuthManager {

	private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

	public void login(User user) {
		currentUser.set(user);
	}

	public void logout() {
		currentUser.remove();
	}

	public User getCurrentUser() {
		checkAuthentication();
		return currentUser.get();
	}

	public boolean isAuthenticated() {
		return currentUser.get() != null;
	}

	public void checkAuthentication() {
		if (currentUser.get() == null) {
			throw new BaseException(
					new ErrorMessage(MessageType.UNAUTHORIZED, "User is not authenticated. Login required."));
		}
	}
}
