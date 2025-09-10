# Gym CRM (Spring Boot, REST API, JPA, & Microservices)

This is a Gym CRM project that has been migrated to a modern Spring Boot microservice architecture. The application now leverages Spring Cloud features for inter-service communication and service discovery. It includes a comprehensive security layer, enhanced observability, and a scalable design.

## Features

- **Spring Boot Microservices:** Converted into a microservice-based architecture, with core functionalities split into dedicated services for better scalability and maintainability.
- **RESTful API:** All functionalities are exposed through a well-defined RESTful API.
- **Full CRUD Operations:** Create, update, delete, and list profiles for Trainees, Trainers, and Trainings.
- **Microservice Integration:**
  - **Trainer Workload Service:** Core workload management logic is now handled by a separate microservice (`trainer-hours-service`).
  - **Feign Client:** The main `gym-crm` service communicates with `trainer-hours-service` via a **declarative Feign client**, simplifying REST API calls.
- **Service Discovery:** Integrated with **Eureka** for dynamic service registration and discovery, eliminating the need for hardcoded service addresses.
- **Authentication & Security:** A complete security layer built with Spring Security and JWT.
  - **Stateless JWT Authentication:** All secured endpoints are protected using a stateless Bearer Token (JWT) mechanism.
  - **Secure Password Storage:** User passwords are never stored in plain text. They are securely salted and hashed using BCryptPasswordEncoder.
  - **Token Blacklisting on Logout:** A robust logout implementation that instantly invalidates JWTs by adding them to a server-side blacklist, preventing reuse.
  - **CORS Policy:** A properly configured CORS policy to allow secure access from frontend applications.
- **Robust Exception Handling:** A centralized `@ControllerAdvice` ensures consistent and clean API error responses across all services.
- **Logging & Observability:**
  - **Actuator:** Enabled for production-ready features like monitoring and managing the application.
  - **Health Indicators:** Implemented custom health checks to verify essential data integrity and application readiness.
  - **Prometheus Metrics:** Added custom metrics for tracking key business events like login attempts and profile creations.
  - **Request Logging Filter:** A custom logging filter captures and logs details of incoming requests and outgoing responses for enhanced debugging and transaction tracing.
- **Environment Support:** Configured **Spring Profiles** to support different environments (`local`, `dev`, `stg`, `prod`) with unique database and server configurations.
- **Unit & Integration Testing:** Comprehensive test coverage using JUnit 5 and Mockito, including profile-specific tests and tests for microservice communication.
- **API Documentation:** The API is documented with OpenAPI 3 (Swagger), improving usability and developer experience.
- **Proper Logging:** Utilizes SLF4J for comprehensive and flexible logging.

## Technologies

- **Java 17+**
- **Spring Boot**
- **Spring Cloud Netflix (for Eureka and Feign)**
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

---

## How to Run

To run the application, you must start each microservice in the specified order to ensure they can communicate correctly.

**Run Order:**

1.  **`discovery-service`**: Start the Eureka Server first. It acts as the central registry for all other services.
2.  **`trainer-hours-service`**: Start the secondary microservice. It will register itself with the Eureka Server.
3.  **`gym-crm`**: Start the main microservice. It will register with Eureka and use the Feign client to discover and communicate with `trainer-hours-service`.