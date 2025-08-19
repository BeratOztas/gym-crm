package com.epam.gym_crm.domain.exception;

public enum MessageType {

	GENERAL_EXCEPTION("9999", "An unexpected error occurred."),
	RESOURCE_NOT_FOUND("1001", "Resource not found."),
	TOKEN_IS_EXPIRED("1014", "Token is expired."),
	UNAUTHORIZED("401","User authentication required"),
	USER_NOT_FOUND("1013","User Not Found."),
	ENTITY_NOT_FOUND("1010","Entity Not Found."),
	PATH_BODY_MISMATCH("1012", "URL path variable does not match the request body identifier."),
	FORBIDDEN("403","You are not authorized."),
	INVALID_STATE("1011","User is not active"),
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
