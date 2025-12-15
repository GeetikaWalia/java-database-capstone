# Smart Clinic Management System

## Section 1: Architecture Summary

The **Smart Clinic Management System** is built on a **three-tier web architecture**, ensuring a clean separation of concerns and scalability. The tiers include:

- **Presentation Tier**: Handles user interaction through **Thymeleaf templates** for server-rendered dashboards (Admin and Doctor) and **REST API consumers** for mobile or web clients.
- **Application Tier**: Powered by **Spring Boot**, this layer contains controllers, services, and business logic. It leverages **Spring MVC** for web views, **REST APIs** for modular communication, and integrates with **Spring Data JPA** and **Spring Data MongoDB** for persistence.
- **Data Tier**: Comprises **MySQL** for structured relational data (patients, doctors, appointments) and **MongoDB** for flexible, document-based data (prescriptions).

This architecture improves **scalability, maintainability, and deployment flexibility**, allowing independent development and scaling of each tier. Spring Boot was chosen for its developer-friendly features, production readiness, and seamless integration with testing, validation, and containerization tools. REST APIs ensure interoperability and future-proofing for mobile and third-party integrations. The system is containerized using **Docker** and supports CI/CD pipelines for automated builds and deployments.

---

## Section 2: Numbered Flow of Data and Control

The following steps describe the flow of data and control across the system:

1. **User Interface Layer**  
   Users interact via **Thymeleaf-based dashboards** (AdminDashboard, DoctorDashboard) or **REST API clients** (mobile apps, patient modules). Dashboards deliver dynamic HTML, while APIs return JSON responses for scalable integrations.

2. **Controller Layer**  
   Requests are routed to **Spring MVC controllers** for server-rendered views or **REST controllers** for API calls. Controllers validate input, coordinate request handling, and delegate logic to services.

3. **Service Layer**  
   Controllers invoke services that apply **business rules**, perform validations, and manage workflows (e.g., checking doctor availability before scheduling). This layer ensures maintainability and testability.

4. **Repository Layer**  
   Services interact with repositories for data access:
   - **MySQL repositories** via Spring Data JPA for structured entities like patients and appointments.
   - **MongoDB repositories** via Spring Data MongoDB for flexible prescription records.

5. **Database Access**  
   Repositories communicate with **MySQL** for normalized relational data and **MongoDB** for unstructured, document-based data, leveraging the strengths of both storage models.

6. **Model Binding**  
   Retrieved data is mapped into Java model classes:
   - **JPA entities** for MySQL tables.
   - **Document objects** for MongoDB collections.
   These models provide a consistent object-oriented representation.

7. **Response Generation**  
   Models are used to generate responses:
   - For MVC flows, models populate Thymeleaf templates for HTML rendering.
   - For REST flows, models or DTOs are serialized into JSON for API clients.

This numbered flow ensures a clear separation of concerns, smooth request-response cycles, and adherence to Spring Boot best practices.
