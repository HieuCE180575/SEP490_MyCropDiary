# MyCropDiary API Starter

Spring Boot structure for the MyCropDiary capstone project. The project uses a
feature-first package layout so each business module owns its API, service,
repository, entity and DTO code instead of putting every controller or entity in
one global folder.

## Technology

- Java 17
- Spring Boot 3.5.x
- Maven
- Spring Web, Validation, Data JPA and Security
- MySQL 8
- Flyway-ready database migration directory
- JWT dependencies
- Swagger/OpenAPI
- Docker Compose for local MySQL

## Source layout

```text
src/main/java/com/mycropdiary/api
├── MyCropDiaryApplication.java
├── common
│   ├── api             # ApiResponse, PageResponse
│   ├── entity          # BaseEntity, auditing fields
│   ├── exception       # Business errors and global handler
│   └── web             # Shared/public endpoints
├── config              # Security, OpenAPI, CORS and application beans
├── security            # JWT parsing, filter and current authenticated user
├── auth                # Register, login, logout, verify/reset password
├── user                # Profile and user account
├── landplot            # Land plot CRUD
├── season              # Crop season, status and dashboard
├── activity            # Cultivation diary and harvest
├── material            # Material usage and quarantine rules
├── expense             # Expenses and cost summaries
├── checklist           # Checklist runs, results and rule evaluation
├── report              # Season PDF report jobs/download
├── ai                  # Conversations, RAG, sources and draft extraction
├── feedback            # Rating, comments and feedback history
└── admin               # Users, crops, rules, KB, feedback reply, stats, audit
```

`landplot` is included as the reference vertical slice with controller, service,
repository, entity and DTO records. The other business APIs intentionally
contain endpoint placeholders labeled with their planned implementation week.

## Local setup

1. Install JDK 17 and Maven 3.9+.
2. Start MySQL:

   ```bash
   docker compose up -d mysql
   ```

3. Import `database/MyCropDiary_MySQL8_Full_Database.sql` using MySQL Workbench.
4. Copy `.env.example` values into the IDE run configuration or environment.
5. Run:

   ```bash
   mvn spring-boot:run
   ```

6. Open Swagger:

   ```text
   http://localhost:8080/api/swagger-ui.html
   ```

Public status endpoint:

```text
GET http://localhost:8080/api/public/status
```

## Implementation order

| Week | Package | Main work |
|---:|---|---|
| 2 | common, config | Response contract, exception, security skeleton, DB mapping |
| 3 | auth, security | Register, verification, login, refresh token, logout |
| 4 | user, auth | Profile, change password, forgot/reset password |
| 5 | landplot | Complete CRUD, search, pagination and archive |
| 6 | season | Season CRUD, lifecycle and dashboard |
| 7 | activity | Diary history and harvest |
| 8 | material | Material usage and pesticide quarantine |
| 9 | expense | Expenses, totals and charts |
| 10 | checklist | Deterministic rule evaluator and result snapshots |
| 11 | report | PDF generation and report history |
| 12 | ai | RAG chat, sources and editable draft extraction |
| 13 | feedback | Ratings, comments, admin reply and notification |
| 14 | admin | Users, crops, rules, KB, statistics and audit logs |
| 15 | all | Integration, security, performance and acceptance tests |

## Important next steps

- Replace `X-Demo-User-Id` in `LandPlotController` with the authenticated user
  obtained from the JWT security context during Week 3.
- Implement `JwtTokenService` and a `OncePerRequestFilter` before using protected
  endpoints.
- Convert placeholder maps into request/response records with Bean Validation.
- Add one service test and one controller integration test per use case.
- Keep ownership checks in backend services even when the UI hides actions.
- Never expose JPA entities directly from completed controllers.

## Branch naming suggestion

```text
feature/authentication
feature/land-plots
feature/crop-seasons
feature/cultivation-diary
feature/material-usage
feature/expense-cost
feature/checklist
feature/season-report
feature/ai-assistant
feature/admin-management
```
