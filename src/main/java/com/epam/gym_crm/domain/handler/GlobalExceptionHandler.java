package com.epam.gym_crm.domain.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.epam.gym_crm.domain.exception.BaseException;
import com.epam.gym_crm.domain.exception.MessageType;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// Application Exception Handler
	@ExceptionHandler(value = { BaseException.class })
	public ResponseEntity<ApiError<?>> handleBaseException(BaseException ex, WebRequest request) {
		MessageType messageType = ex.getErrorMessage().getMessageType();

		logger.warn("Handling BaseException. MessageType: {}, Details: {}", messageType, ex.getMessage());

		HttpStatus status;

		switch (messageType) {
		case RESOURCE_NOT_FOUND:
		case ENTITY_NOT_FOUND:
		case USER_NOT_FOUND:
			status = HttpStatus.NOT_FOUND;
			break;

		case UNAUTHORIZED:
		case TOKEN_IS_EXPIRED:
			status = HttpStatus.UNAUTHORIZED;
			break;

		case FORBIDDEN:
			status = HttpStatus.FORBIDDEN;
			break;

		case INVALID_ARGUMENT:
		case VALIDATION_ERROR:
		case PATH_BODY_MISMATCH:
			status = HttpStatus.BAD_REQUEST;
			break;

		case DUPLICATE_USERNAME:
		case INVALID_STATE: 
		case ASSOCIATION_ERROR: 
			status = HttpStatus.CONFLICT;
			break;

		case GENERAL_EXCEPTION:
		default:
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			break;
		}

		return ResponseEntity.status(status).body(createApiError(status, ex.getMessage(), request));
	}

	// Validation Exception Handler
	@ExceptionHandler(value = { MethodArgumentNotValidException.class })
	public ResponseEntity<ApiError<Map<String, List<String>>>> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException ex, WebRequest request) {
		logger.warn("Handling MethodArgumentNotValidException for path {}: {}", request.getDescription(false),
				ex.getMessage());

		Map<String, List<String>> errorsMap = new HashMap<>();

		for (ObjectError objError : ex.getBindingResult().getAllErrors()) {
			String fieldName = ((FieldError) objError).getField();
			String message = objError.getDefaultMessage();
			if (errorsMap.containsKey(fieldName)) {
				errorsMap.get(fieldName).add(message);
			} else {
				errorsMap.put(fieldName, new ArrayList<>(Arrays.asList(message)));
			}
		}

		return ResponseEntity.badRequest().body(createApiError(HttpStatus.BAD_REQUEST, errorsMap, request));
	}

	public <E> ApiError<E> createApiError(HttpStatus httpStatus, E message, WebRequest request) {
		ApiError<E> apiError = new ApiError<>();
		apiError.setStatus(httpStatus.value());

		Exception<E> exception = new Exception<>();
		String requestPath = request.getDescription(false);
		if (requestPath.startsWith("uri=")) {
			exception.setPath(requestPath.substring(4));
		} else {
			exception.setPath(requestPath);
		}
		exception.setCreateTime(new Date());
		exception.setHostName(getHostName());
		exception.setMessage(message);

		apiError.setException(exception);
		return apiError;

	}

	private String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("Unable to determine host name for error response.", e);
			return "unknown-host";
		}
	}

}