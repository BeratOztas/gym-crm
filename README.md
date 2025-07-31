# Gym CRM (Spring Core, REST API & Hibernate)

This is a simple Gym CRM (Customer Relationship Management) project built using **Spring Core and Spring MVC**. It provides a RESTful API to manage Trainee, Trainer, and Training profiles. The application uses Hibernate ORM for persistence with a PostgreSQL database.

## Features

- **RESTful API:** All functionalities are now exposed through a well-defined RESTful API.
- **Full CRUD Operations:** Create, update, delete, and list profiles for Trainees, Trainers, and Trainings.
- **Advanced Search:** Search trainings by various criteria like name, type, date, trainer, or trainee.
- **Authentication:** Secure user login and password management.
- **Security:** Session-based security with automatic logout after each request.
- **Robust Exception Handling:** A centralized `GlobalExceptionHandler` ensures consistent and clean API error responses.
- **Dynamic Logging:** A custom filter logs all incoming requests and outgoing responses, with a unique transaction ID for each interaction.
- **Java-based Configuration:** The entire application is configured with Java annotations, eliminating the need for XML.
- **Embedded Tomcat:** The application runs directly from the `main` method using a programmatically configured embedded Tomcat server.
- **Test Coverage:** All business logic is covered by unit tests (JUnit 5 + Mockito).
- **Proper Logging:** Utilizes SLF4J for comprehensive and flexible logging.

## Technologies

- **Java 17+**
- **Spring Framework:**
    - **Spring Core:** For dependency injection and IoC container.
    - **Spring MVC:** To build the RESTful API layer.
- **Hibernate ORM**
- **PostgreSQL**
- **Embedded Tomcat**
- **Lombok**
- **JUnit 5 & Mockito**
- **SLF4J & Log4j2**

**Note:** This project was developed as part of an EPAM training to demonstrate Spring Core architecture, REST API design, and modern web application configuration principles.