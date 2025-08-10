package com.epam.gym_crm.exception;

public class BaseException extends RuntimeException {

	private static final long serialVersionUID = 5674758076774251773L;

	private final ErrorMessage errorMessage;

	public BaseException(ErrorMessage errorMessage) {
		super(errorMessage.prepareErrorMessage());
		this.errorMessage = errorMessage;
	}

	public ErrorMessage getErrorMessage() {
		return errorMessage;
	}
}
