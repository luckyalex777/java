# User CRUD service

Plain Spring MVC and Hibernate WAR service for managing users. It does not use Spring Boot.

## Endpoints

- `GET /api/users`
- `GET /api/users/{id}`
- `POST /api/users` with `{"loginName":"alex","password":"secret"}`
- `PUT /api/users/{id}` with the same request body
- `DELETE /api/users/{id}`

Set `X-User` on create and update requests to populate the audit user. The response never returns the password.

Passwords are stored as salted `PBKDF2WithHmacSHA256` hashes in the `password_hash` column. Existing plaintext values must be migrated by resetting each password through the create/update service flow; they cannot be verified against the new hash format.

## Run

Create the `user_service` MySQL database and apply `src/main/resources/schema.sql`. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`, then build with `mvn -f pom.xml verify`. Deploy the resulting WAR to a Jakarta Servlet 6 compatible container.