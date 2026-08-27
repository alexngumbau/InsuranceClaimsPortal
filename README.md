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

- `InsuranceClaimsBE`: Spring Boot, Spring Web MVC, Spring Data JPA, Bean Validation, and MySQL.
- `InsuranceClaimsFE`: React, TypeScript, Vite, and Ant Design.
- The backend owns validation, persistence, search/filtering, metrics, and status-transition rules.
- The frontend communicates with the backend through `/api` endpoints.

## Requirements

- Java 17 or newer
- Maven Wrapper (included)
- Node.js and npm
- MySQL 8 or compatible MySQL server

## Database Setup

Create the database before starting the backend:

```sql
CREATE DATABASE insuranceClaims;
```

The application expects a MySQL user with access to this database. Do not commit database passwords or other credentials.

## Backend Setup

The backend configuration uses Spring profiles. `application.properties` selects the active profile and configures the server port. Profile files read database settings from environment variables.

Copy the safe template and edit it locally:

```bash
cd InsuranceClaimsBE
cp .env.example .env
```

Set the values in `.env`:

```env
SPRING_PROFILES_ACTIVE=local
DB_URL=jdbc:mysql://localhost:3306/insuranceClaims
DB_USERNAME=replace-with-your-mysql-username
DB_PASSWORD=replace-with-your-mysql-password
JPA_DDL_AUTO=update
```

Spring Boot does not automatically load a file named `.env`. Export the values before starting the application:

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

The API runs on `http://localhost:8080` by default.

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

For production, use the `prod` profile and provide explicit environment variables. Use `JPA_DDL_AUTO=validate` with a separately managed database schema.

## Frontend Setup

Install dependencies:

```bash
cd InsuranceClaimsFE
npm install
```

Create a local frontend environment file if needed:

```bash
cp .env.example .env.local
```

Set the public backend URL:

```env
VITE_API_URL=http://localhost:8080 - this is for local setup only
```

Start the development server:

```bash
npm run dev -- --mode local
```

The frontend is typically available at `http://localhost:5173`.

Vite environment files are mode-specific:

- `.env.development` is used by the normal development mode.
- `.env.local` is used for machine-specific local overrides.
- `.env.production` is used for production builds.
- Only variables prefixed with `VITE_` are exposed to browser code. Never put private credentials in frontend environment files.

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

Backend tests:

```bash
cd InsuranceClaimsBE
set -a
source .env
set +a
./mvnw test
```

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

- Real `.env` files and local profile files are ignored by Git.
- `.env.example` files contain placeholders only.
- Database credentials must be supplied through the runtime environment.
- CORS is configured for local frontend development.
- Authentication and role-based authorization are not currently implemented.
- Production deployments should use a secrets manager or platform-provided environment variables.

## Assumptions and Trade-offs

- This is a focused interview MVP rather than a production-ready claims platform.
- MySQL is used for local development and persistence.
- Hibernate schema update mode is convenient for the assessment; production should use controlled migrations such as Flyway or Liquibase.
- Metrics currently aggregate claims in the service layer. For a large portfolio, database-side aggregation and indexing should be introduced.
- Status transitions are enforced in the backend service and are not configurable by users.

## Known Limitations

- No authentication, authorization, or claims-officer identity is stored.
- No status-history or audit-log table is currently implemented.
- Claim editing and deletion are not exposed as general CRUD operations.
- Controller-level API contract tests and frontend component tests remain future improvements.
