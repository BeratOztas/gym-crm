package com.epam.gym_crm.exception;

public enum MessageType {

	GENERAL_EXCEPTION("9999", "An unexpected error occurred."),
	RESOURCE_NOT_FOUND("1001", "Resource not found."),
	VALIDATION_ERROR("1003", "Validation failed."),
	INVALID_ARGUMENT("1002", "Invalid parameter provided."),
	DUPLICATE_USERNAME("1004", "Username already exists."),
	ASSOCIATION_ERROR("1005", "Related resource not found or invalid.");

	private String code;
	private String message;

	private MessageType(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
