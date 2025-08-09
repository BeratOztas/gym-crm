package com.epam.gym_crm.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class AppConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().openapi("3.0.1")
				.info(new Info().title("Gym CRM API (OpenAPI 3)").version("1.0.0")
						.description("Spring Boot REST API Documents.")
						.contact(new Contact().name("Berat").email("berat.oztas.dev@gmail.com"))
						.license(new License().name("Apache 2.0").url("http://springdoc.org")));
	}

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder().group("gym-crm-public").pathsToMatch("/api/**").build();
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		return mapper;
	}

}
