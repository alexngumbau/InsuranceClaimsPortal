# Insurance Claims Processing Portal

A full-stack MVP for managing insurance claims. Claims officers can review claims, search and filter the claims portfolio, create claims against existing policies, inspect claim details, and advance claims through the supported workflow.

## Architecture

```text
InsuranceClaimsFE (React + TypeScript + Vite)
              |
              | HTTP/JSON
              v
InsuranceClaimsBE (Spring Boot REST API)
              |
              v
           MySQL
```

- `InsuranceClaimsBE`: Spring Boot, Spring Web MVC, Spring Data JPA, Bean Validation, Liquibase migrations, and MySQL.
- `InsuranceClaimsFE`: React, TypeScript, Vite, and Ant Design.
- The backend owns validation, persistence, search/filtering, metrics, and status-transition rules.
- The frontend communicates with the backend through `/api` endpoints.

## Quick Start

The entire stack — MySQL, the Spring Boot API, and the frontend — runs with Docker Compose. No local Java, Node.js, or MySQL installation is required.

Prerequisites:

- Docker Desktop (includes Docker Compose v2)

Create the environment file from the safe template and replace the placeholder database passwords:

```bash
cp .env.example .env
```

Build and start all services:

```bash
docker compose up --build -d
```

On first start, Liquibase migrates the empty database and the backend seeds sample policies and claims automatically.

Open the application at `http://localhost:5173`. The API is available at `http://localhost:8080`, and Swagger UI at `http://localhost:8080/swagger-ui.html`. MySQL is exposed on host port `3307` for connecting a database client.

Useful commands:

```bash
docker compose logs -f     # follow service logs
docker compose down        # stop the stack (data survives in the mysql_data volume)
docker compose down -v     # stop and wipe the database; it is re-migrated and re-seeded on the next start
```

The frontend build uses `VITE_API_URL=http://localhost:8080` because API requests are made by the user's browser. The backend uses the Compose service name `mysql` for its internal database connection.

## Database Migrations (Liquibase)

The database schema is owned by [Liquibase](https://www.liquibase.org/) and applied automatically when the backend starts. Hibernate no longer creates or alters tables: it runs in `validate` mode and fails fast if the JPA entities and the migrated schema ever drift apart.

- Master changelog: `InsuranceClaimsBE/src/main/resources/db/changelog/db.changelog-master.xml`
- Changesets:
  - `001-create-policy-table` — creates `policies` with a unique `policy_number`
  - `002-create-claim-table` — creates `claims` with a unique `claim_number` and a `DECIMAL(19,2)` amount column
  - `002-add-claim-policy-fk` — enforces `claims.policy_id -> policies.id`
  - `002-add-claim-search-indexes` — indexes on `status`, `created_at`, and `policy_id`
- Applied changesets are recorded in the `DATABASECHANGELOG` table, so every environment converges to the same schema in the same order.

To evolve the schema, add a new changeset and include it from the master changelog. Do not modify changesets that were already applied:

```xml
<changeSet id="003-add-claim-assigned-to" author="you">
    <addColumn tableName="claims">
        <column name="assigned_to" type="VARCHAR(255)"/>
    </addColumn>
</changeSet>
```

## API Documentation

When the backend is running, interactive Swagger UI is available at:

```text
http://localhost:8080/swagger-ui.html
```

The generated OpenAPI document is available at:

```text
http://localhost:8080/v3/api-docs
```

Swagger documentation is generated from the backend controllers and includes claim and policy operations, request validation metadata, status values, and documented error responses.

## Configuration

All backend configuration is supplied through environment variables — there are no Spring profile files. `application.properties` maps them to Spring settings, so the same JAR runs anywhere; only the injected values differ. Docker Compose injects all of them from the root `.env`:

| Variable | Purpose |
| --- | --- |
| `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD` | Bootstrap the MySQL container and derive the backend credentials |
| `DB_URL` | JDBC connection string used by the backend (`jdbc:mysql://mysql:3306/$MYSQL_DATABASE`) |
| `DB_USERNAME`, `DB_PASSWORD` | Database credentials used by the backend (from `MYSQL_USER` / `MYSQL_PASSWORD`) |
| `SERVER_PORT` | API port (default `8080`) |
| `JPA_DDL_AUTO` | Hibernate schema mode (default `validate`) |
| `VITE_API_URL` | Backend URL baked into the frontend bundle at build time (default `http://localhost:8080`) |

For production, provide all environment variables explicitly. Liquibase applies the schema on startup and Hibernate validates it (`JPA_DDL_AUTO=validate` by default), so no separate schema-management step is required.

## API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/claims` | Create a claim with `SUBMITTED` status. |
| `GET` | `/api/claims` | List claims with pagination, search, status, and type filters. |
| `GET` | `/api/claims/{id}` | Return claim details and related policy information. |
| `GET` | `/api/claims/metrics` | Return total, pending, approved amount, and paid claim metrics. |
| `PATCH` | `/api/claims/{id}/status` | Change a claim status after validating the transition. |
| `GET` | `/api/policies/{policyNumber}` | Look up a policy for claim creation. |

The claims list supports Spring pagination parameters such as `page`, `size`, and sorting. Search is provided through `search`; optional filters use `status` and `type`.

Example request:

```bash
curl "http://localhost:8080/api/claims?page=0&size=10&search=Jane"
```

## Claim Rules

Supported claim types:

- `Motor`
- `Health`
- `Travel`
- `Property`
- `Other`

Supported statuses:

- `SUBMITTED`
- `UNDER_REVIEW`
- `APPROVED`
- `REJECTED`
- `PAID`

Allowed status transitions:

```text
SUBMITTED   -> UNDER_REVIEW
UNDER_REVIEW -> APPROVED or REJECTED
APPROVED    -> PAID
REJECTED    -> terminal
PAID        -> terminal
```

Claim creation also checks that the claim number is unique, the referenced policy exists, and the customer and claim type match the policy.

## Testing and Quality Checks

Backend tests (requires a local JDK 17; the Maven Wrapper is included):

```bash
cd InsuranceClaimsBE
set -a
source .env
set +a
./mvnw test
```

The Spring context test boots the full application, so the database environment variables from `InsuranceClaimsBE/.env` (template: `.env.example`) must be exported first, as shown above.

The backend test suite covers claim creation, duplicate claim numbers, missing policies, policy/customer and policy/type mismatches, and valid and invalid status transitions.

Frontend build:

```bash
cd InsuranceClaimsFE
npm run build
```

Frontend lint:

```bash
npm run lint
```

The current backend suite contains 10 passing tests. The frontend TypeScript build and ESLint checks also pass.

## Security and Configuration Notes

- Real `.env` files are ignored by Git.
- `.env.example` files contain placeholders only.
- Database credentials must be supplied through the runtime environment.
- CORS is configured for local frontend development.
- Authentication and role-based authorization are not currently implemented.
- Production deployments should use a secrets manager or platform-provided environment variables.

## Assumptions and Trade-offs

- This is a focused interview MVP rather than a production-ready claims platform.
- MySQL runs as a Docker Compose service for persistence.
- The schema is managed by Liquibase migrations and Hibernate runs with `ddl-auto=validate`, so schema changes are versioned, reviewable, and reproducible across environments.
- Metrics currently aggregate claims in the service layer. For a large portfolio, database-side aggregation would scale better. Basic indexes on `status`, `created_at`, and `policy_id` are already created by the migrations.
- Status transitions are enforced in the backend service and are not configurable by users.

## Known Limitations

- No authentication, authorization, or claims-officer identity is stored.
- No status-history or audit-log table is currently implemented.
- Claim editing and deletion are not exposed as general CRUD operations.
- Controller-level API contract tests and frontend component tests remain future improvements.
