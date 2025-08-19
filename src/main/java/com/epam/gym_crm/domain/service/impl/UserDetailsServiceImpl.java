package com.epam.gym_crm.domain.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.epam.gym_crm.db.entity.User;
import com.epam.gym_crm.db.repository.UserRepository;
import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.ErrorMessage;
import com.epam.gym_crm.domain.exception.MessageType;
import com.epam.gym_crm.security.JwtUserDetails;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;
	private final LoginAttemptService loginAttemptService;

	public UserDetailsServiceImpl(UserRepository userRepository,LoginAttemptService loginAttemptService) {
		this.userRepository = userRepository;
		this.loginAttemptService=loginAttemptService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		User user = userRepository.findByUsername(username).orElseThrow(
				() -> new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "Username : " + username)));
		
		boolean isAccountNonLocked = !loginAttemptService.isBlocked(username);
		
		return JwtUserDetails.create(user,isAccountNonLocked);
	}
	
	public UserDetails loadUserById(Long id) {
		User user =userRepository.findById(id)
				.orElseThrow(() ->new BaseException(new ErrorMessage(MessageType.USER_NOT_FOUND, "User Id : "+id)));
		return JwtUserDetails.create(user, true);
	}

}
