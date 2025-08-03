package com.epam.gym_crm.config;

import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.epam.gym_crm.auth.AuthManager;
import com.epam.gym_crm.filter.RequestLoggingFilter;
import com.epam.gym_crm.filter.SessionManagementFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	private final ObjectMapper objectMapper;

	public WebConfig(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Bean
	public SessionManagementFilter sessionManagementFilter(AuthManager authManager) {
		return new SessionManagementFilter(authManager);
	}

	@Bean
	public RequestLoggingFilter requestLoggingFilter() {
		return new RequestLoggingFilter();
	}

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().openapi("3.0.1")
				.info(new Info().title("Gym CRM API (OpenAPI 3)").version("1.0.0")
						.description("Spring Core  REST API Documents.")
						.contact(new Contact().name("Berat").email("berat.oztas.dev@gmail.com"))
						.license(new License().name("Apache 2.0").url("http://springdoc.org")));
	}

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder().group("gym-crm-public").pathsToMatch("/api/**").build();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");

		registry.addResourceHandler("/swagger-ui/**")
				.addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/4.15.5/");

		registry.addResourceHandler("/v3/api-docs/**").addResourceLocations("classpath:/META-INF/resources/");
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
	}

	@Override
	public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
		configurer.favorParameter(false).ignoreAcceptHeader(false).defaultContentType(MediaType.APPLICATION_JSON);
	}
}