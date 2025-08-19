# Gym CRM (Spring Boot, REST API , JPA & Spring Security)

This is a Gym CRM project that has been migrated from Spring Core to Spring Boot. The migration leverages Spring Boot's conventions and auto-configuration to simplify development and deployment. The application now includes a comprehensive Spring Security layer for robust authentication and authorization, along with enhanced observability features with Actuator and Prometheus metrics.

## Features

- **Spring Boot Application:** Converted to a modern Spring Boot application, simplifying configuration and deployment.
- **RESTful API:** All functionalities are exposed through a well-defined RESTful API.
- **Full CRUD Operations:** Create, update, delete, and list profiles for Trainees, Trainers, and Trainings.
- **Authentication & Security:** A complete security layer built with Spring Security and JWT.
     - **Stateless JWT Authentication:** All secured endpoints are protected using a stateless Bearer Token (JWT) mechanism.
     - **Secure Password Storage:**  User passwords are never stored in plain text. They are securely salted and hashed using BCryptPasswordEncoder.
     - **Brute-Force Protection:** The login endpoint is protected against password guessing attacks. An account is temporarily locked for 5 minutes after 3 unsuccessful login attempts.
     - **Token Blacklisting on Logout:** A robust logout implementation that instantly invalidates JWTs by adding them to a server-side blacklist, preventing reuse.
     - **CORS Policy:** A properly configured CORS policy to allow secure access from frontend applications.
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
- **Spring Security (with JWT)**
- **Spring Boot Actuator & Micrometer**
- **Prometheus**
- **Hibernate ORM**
- **PostgreSQL**
- **Google Guava (for in-memory caching)**
- **Lombok**
- **JUnit 5 & Mockito**
- **SLF4J & Log4j2**

**Note:** This project demonstrates a complete security implementation on top of a modern Spring Boot architecture, focusing on key features like JWT-based authentication, brute-force protection, secure logout, observability, and robust testing.