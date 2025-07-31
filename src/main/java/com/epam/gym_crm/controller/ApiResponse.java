package com.epam.gym_crm.controller;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private Integer status;
	private T payload;
	private String errorMessage;

	public static <T> ApiResponse<T> ok(T payload) {
		ApiResponse<T> response = new ApiResponse<>();
		response.setStatus(HttpStatus.OK.value());
		response.setPayload(payload);
		response.setErrorMessage(null);
		return response;
	}

	public static <T> ApiResponse<T> error(String errorMessage) {
		ApiResponse<T> response = new ApiResponse<>();
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		response.setErrorMessage(errorMessage);
		response.setPayload(null);
		return response;
	}

}
