# HR Management System - Coding Standard

This document details the coding standards and best practices for the HR Management System. Consistency in code style, error handling, and component design is critical for maintaining this Spring Boot application.

## 1. Java Code Style

- **Java Version**: Java 21 features should be utilized appropriately (Records, Pattern Matching, Text Blocks, etc.).
- **Indentation**: Use 4 spaces for indentation (no tabs).
- **Braces**: Use standard K&R style brackets (opening brace on the same line as the declaration).
- **Imports**: Avoid wildcard imports (`import java.util.*;`). Explicitly import classes. Separate external dependencies, framework imports, and project imports with blank lines.

## 2. Lombok Usage Guidelines

Lombok is used to reduce boilerplate code, but it must be used thoughtfully.

**Recommended Annotations:**
- `@Getter`, `@Setter` (for Entities, though prefer careful setter exposure)
- `@NoArgsConstructor`, `@AllArgsConstructor`
- `@RequiredArgsConstructor` (for Spring dependency injection)
- `@Builder` (for test data creation or complex object construction)
- `@Slf4j` (for logging)

**Avoid or Use with Caution:**
- `@Data`: Avoid on Entities! It implements `equals()`, `hashCode()`, and `toString()` involving all fields, which can cause lazy loading issues, infinite recursion (StackOverflowError) with bidirectional relationships, and poor performance.
- `@EqualsAndHashCode`: Be very careful when using this with JPA Entities. Only include business keys, not database IDs or collections.
- `@ToString`: Always exclude lazy-loaded collections and password fields using `@ToString.Exclude`.

## 3. DTO Design Patterns

- **Records**: Use Java 14+ `record` for most DTOs, especially for Responses and Requests. Records are immutable and concise.
  ```java
  public record EmployeeResponse(
      Long id,
      String code,
      String fullName,
      String departmentName
  ) {}
  ```
- **Classes**: Use standard POJO classes only when mutability is strictly required by a specific framework mechanism before processing, though this is rare in modern Spring Boot.
- **Separation**: Keep Request DTOs and Response DTOs completely separate to avoid exposing unintended data or accepting unintended updates.

## 4. Exception Handling Patterns

We use a Global Exception Handler (`@RestControllerAdvice`) to translate exceptions into standardized `ApiResponse` objects.

**Custom Exception Pattern:**
```java
public class ResourceNotFoundException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.RESOURCE_NOT_FOUND;
    }
}
```

**Controller Advice Pattern:**
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getMessage()));
    }
    
    // Catch-all for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.getCode(), "An unexpected error occurred"));
    }
}
```

## 5. API Versioning Strategy

- **URI Versioning**: APIs must be versioned in the URL path.
- **Format**: `/api/v{major}/{resource}` (e.g., `/api/v1/employees`).
- Minor or patch changes (non-breaking) do not require a new version number.
- Breaking changes require a new major version (e.g., moving to `/api/v2/employees`).

## 6. Logging Standards

- **Framework**: Use SLF4J with Logback (`@Slf4j` via Lombok).
- **Levels**:
  - `ERROR`: Exceptions that prevent a flow from completing (e.g., DB connection failure, critical integration failure).
  - `WARN`: Issues that are handled but shouldn't happen regularly (e.g., invalid tokens, retries, resource not found).
  - `INFO`: Significant business events (e.g., payroll processed, user registered, application started).
  - `DEBUG`: Detailed flow information useful for troubleshooting during development.
- **Context**: Always include relevant IDs in logs (e.g., `userId`, `employeeId`) but **NEVER** log sensitive information like passwords, tokens, or PII.

## 7. Comment Guidelines

- Code should be self-documenting. Use comments to explain *why* something is done, not *what* is done.
- Use Javadoc for all public API methods, interfaces, and complex classes.
- Avoid leaving commented-out code in the repository.

## 8. Class and Method Guidelines

- **Method Length**: Aim for methods under 30-40 lines. If a method is longer, extract private helper methods.
- **Class Responsibility**: A class should not exceed 500 lines. If a service becomes too large, decompose it into smaller, more focused services or use cases.
- **Parameters**: Avoid methods with more than 4 parameters. If needed, encapsulate parameters in an object.

## 9. Package Structure (Per Feature)

Each feature module must adhere to the following internal structure:

```
feature_name/
├── controller/       # REST endpoints (@RestController)
├── service/          # Interfaces and implementations (@Service)
│   └── impl/
├── repository/       # Data access interfaces (@Repository)
├── entity/           # JPA Entity classes (@Entity)
├── dto/              # Data Transfer Objects
│   ├── request/
│   └── response/
└── mapper/           # MapStruct interfaces (@Mapper)
```

## 10. Validation Patterns

- Use `jakarta.validation.constraints` (e.g., `@NotBlank`, `@Size`, `@Pattern`) directly on Request DTO fields.
- Trigger validation in the controller using `@Valid`.
- Do not perform complex business validation in DTOs. Complex validation (e.g., checking if a username is unique in the database) belongs in the Service layer.

## 11. MapStruct Mapping Guidelines

- Define mappers as interfaces annotated with `@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)`.
- Use explicit `@Mapping` annotations when field names differ.
- Do not put business logic inside mappers.

## 12. Query Optimization Guidelines

- **N+1 Problem**: Always be vigilant about the N+1 select problem. Use `@EntityGraph` or `JOIN FETCH` in JPQL queries when you need to load related entities.
- **Pagination**: Never return unbounded collections from the database. Use Spring Data's `Pageable` and `Page<T>` for lists.
- **Projections**: If you only need a few columns, use Interface Projections or Constructor Expressions (DTO projections) rather than fetching full heavy Entities.
- **Indexing**: Ensure appropriate database indexes are defined via Flyway migrations for frequently queried columns.
