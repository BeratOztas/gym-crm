# Gym CRM (Spring Boot, REST API & JPA)

This is a **Gym CRM** project that has been migrated from Spring Core to **Spring Boot**. The migration leverages Spring Boot's conventions and auto-configuration to simplify development and deployment. The application now includes enhanced observability features with **Actuator**, custom health indicators, and Prometheus metrics.

## Features

- **Spring Boot Application:** Converted to a modern Spring Boot application, simplifying configuration and deployment.
- **RESTful API:** All functionalities are exposed through a well-defined RESTful API.
- **Full CRUD Operations:** Create, update, delete, and list profiles for Trainees, Trainers, and Trainings.
- **Authentication & Security:** Enhanced security with password encoding and Spring Security integration for user authentication.
- **Robust Exception Handling:** A centralized `@ControllerAdvice` ensures consistent and clean API error responses.
- **Observability:**
    - **Actuator:** Enabled for production-ready features like monitoring and managing the application.
    - **Health Indicators:** Implemented custom health checks to verify essential data integrity and application readiness.
    - **Prometheus Metrics:** Added custom metrics for tracking key business events like login attempts and profile creations.
- **Environment Support:** Configured **Spring Profiles** to support different environments (`local`, `dev`, `stg`, `prod`) with unique database and server configurations.
- **Unit & Integration Testing:** Comprehensive test coverage using JUnit 5 and Mockito, including profile-specific tests.
- **Proper Logging:** Utilizes SLF4J for comprehensive and flexible logging.

## Technologies

- **Java 17+**
- **Spring Boot**
- **Spring Data JPA**
- **Spring Security**
- **Spring Boot Actuator & Micrometer**
- **Prometheus**
- **Hibernate ORM**
- **PostgreSQL**
- **Lombok**
- **JUnit 5 & Mockito**
- **SLF4J & Log4j2**

**Note:** This project demonstrates the migration of a legacy Spring Core application to a modern Spring Boot architecture, focusing on key features like observability, configuration management, and robust testing.