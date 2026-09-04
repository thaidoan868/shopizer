## Release v0.1 - Core Infra & Authentication

### New Features

customers can register new accounts at `http://localhost:8080/realms/shopizer/account`
They have to verify their email address before they can log in.
They can change their password and update their email address in keycloak dashboard.

users can update their avatars at `/api/v1/users/me/avatar` endpoint.
customers can get and update their profile information at `/api/v1/customers/me/profile` endpoint.
employees can get and update their profile information at `/api/v1/employees/me/profile` endpoint.
the super admin can create new employees in the keycloak dashboard.

### Infrastructure

Set up the following services:

- Keycloak for authentication and authorization.
- PostgreSQL for database.
- MinIo for object storage.
- RabbitMQ for message queue.