package com.epam.gym_crm.exception;

public class BaseException extends RuntimeException {
	
	public BaseException(ErrorMessage errorMessage) {
		super(errorMessage.prepareErrorMessage());
	}
}
