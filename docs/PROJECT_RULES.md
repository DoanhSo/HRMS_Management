# HR Management System - Project Rules

This document outlines the core architectural principles, coding rules, naming conventions, and workflow standards for the HR Management System. All team members must adhere to these guidelines to ensure consistency, maintainability, and code quality.

## Architecture Rules

1. **Package-by-Feature**: The project is structured using the package-by-feature pattern. Each feature module (e.g., `employee`, `attendance`) is self-contained and includes its own `controller`, `service`, `repository`, `dto`, `entity`, and `mapper` packages.
2. **No Circular Dependencies**: Feature modules must not have circular dependencies on each other. If shared logic is needed, extract it into common utility components or use event-driven communication if appropriate.
3. **Shared Common Module**: The `common` package (containing config, security, exception, audit, validation, and utils) is accessible to all feature modules.
4. **Strict Layering**: The request flow must strictly follow: `Controller` → `Service` → `Repository`. Never skip layers (e.g., a Controller should never call a Repository directly).
5. **Entity Isolation**: Entity classes must **NEVER** be exposed outside the service layer. Always map entities to DTOs before returning them to the controller.

## Coding Rules

1. **No Hardcoded Business Values**: **NEVER** hardcode business values. Use configuration tables in the database or `application.yaml`.
2. **No Hardcoded Salary Formulas**: **NEVER** hardcode salary formulas or logic. Use the `salary_rules` table and the Strategy/Rule Engine pattern to process payroll dynamically.
3. **No Hardcoded Secrets**: **NEVER** hardcode JWT secrets, database credentials, or API keys. Always externalize them to environment variables.
4. **Always Return DTOs**: **NEVER** return Entity objects directly from controllers. Always return standardized DTOs wrapped in an `ApiResponse<T>`.
5. **Constructor Injection**: Always use constructor injection for Spring beans (or Lombok's `@RequiredArgsConstructor`). **NO** `@Autowired` on fields.
6. **Thin Controllers**: Controllers contain **NO** business logic. They are strictly for request mapping, input validation, invoking service methods, and structuring the response.
7. **Fat Services**: Services contain **ALL** business logic and orchestration.
8. **Dumb Repositories**: Repositories are strictly for database access operations. They should not contain any business logic or application state.
9. **Follow SOLID Principles**:
   - **S**ingle Responsibility Principle: A class should have one, and only one, reason to change.
   - **O**pen-Closed Principle: Software entities should be open for extension, but closed for modification.
   - **L**iskov Substitution Principle: Objects in a program should be replaceable with instances of their subtypes without altering the correctness of that program.
   - **I**nterface Segregation Principle: Many client-specific interfaces are better than one general-purpose interface.
   - **D**ependency Inversion Principle: Depend upon abstractions, not concretions.
10. **Follow Clean Code**: Write readable, maintainable, and self-documenting code.
11. **Meaningful Naming**: Use clear, descriptive names for variables, methods, and classes. Avoid abbreviations.
12. **DRY (Don't Repeat Yourself)**: Avoid duplicated code. Extract common logic into reusable methods or components.
13. **No Magic Numbers**: Avoid arbitrary numbers or strings in code. Define them as `static final` constants or `enum` types.
14. **Clear Public API**: Every `public` method must have a well-defined purpose and ideally a Javadoc comment explaining its contract.
15. **Composition > Inheritance**: Prefer object composition over class inheritance for code reuse.
16. **Safe Optional Usage**: Use `Optional` properly. Never call `.get()` without first checking `.isPresent()` or using functional methods like `.orElseThrow()`.
17. **Swagger Documentation**: All API endpoints must be documented using OpenAPI/Swagger annotations (`@Tag`, `@Operation`, etc.).
18. **MapStruct**: Use MapStruct interfaces for mapping between Entities and DTOs to avoid boilerplate mapping code.
19. **Transaction Management**: Use `@Transactional` appropriately on service methods to ensure data integrity.
20. **Specific Exception Handling**: Never catch generic `Exception` or `RuntimeException`. Always catch specific exceptions (e.g., `EntityNotFoundException`) and handle them appropriately.

## Naming Conventions

- **Entities**: Singular nouns representing database tables (e.g., `Employee`, `Department`, `LeaveRequest`).
- **DTOs**: Suffix with the operation/purpose (e.g., `EmployeeCreateRequest`, `EmployeeResponse`, `EmployeeUpdateRequest`).
- **Services**: Interface named `EntityService` (e.g., `EmployeeService`), implementation named `EntityServiceImpl` (e.g., `EmployeeServiceImpl`).
- **Repositories**: Suffix with `Repository` (e.g., `EmployeeRepository`).
- **Controllers**: Suffix with `Controller` (e.g., `EmployeeController`).
- **Mappers**: Suffix with `Mapper` (e.g., `EmployeeMapper`).
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_COUNT`).
- **API Paths**: kebab-case, plural nouns, versioned (e.g., `/api/v1/employees`, `/api/v1/leave-requests`).

## Git Conventions

- **Conventional Commits**: Commit messages must follow the conventional commits specification:
  - `feat:` A new feature
  - `fix:` A bug fix
  - `refactor:` A code change that neither fixes a bug nor adds a feature
  - `docs:` Documentation only changes
  - `test:` Adding missing tests or correcting existing tests
  - `chore:` Changes to the build process or auxiliary tools and libraries
- **Branch Naming**:
  - Features: `feature/module-name` (e.g., `feature/payroll-calculation`)
  - Bugfixes: `bugfix/issue-description` (e.g., `bugfix/fix-leave-balance-update`)

## Testing Rules

- **Unit Tests**: Mandatory for all service layer methods containing business logic. Use Mockito for mocking dependencies.
- **Integration Tests**: Write for repositories to verify custom queries and database constraints.
- **Controller Tests**: Test REST endpoints using `MockMvc` to verify routing, status codes, and JSON serialization.
- **Coverage**: Maintain a minimum of **80%** code coverage across the project.
