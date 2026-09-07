# E-Commerce Shopizer

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Keycloak](https://img.shields.io/badge/Keycloak-26.0-blue.svg)](https://www.keycloak.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange.svg)](https://www.rabbitmq.com/)
[![MinIO](https://img.shields.io/badge/MinIO-S3-red.svg)](https://min.io/)

## Overview

**Shopizer** is a microservice-based e-commerce backend platform designed specifically for selling computer components.
Built with **Java 21** and **Spring Boot**, it is engineered as a production-ready, highly available distributed system
featuring automated CI/CD pipelines, containerized deployments, robust messaging, and comprehensive testing suites.

---

## Architecture & Tech Stack

* **Language & Framework:** Java 21, Spring Boot 3.5, Maven (Multi-module)
* **API Gateway:** Spring Cloud Gateway (Reactive request routing & load balancing)
* **Identity & Access Management:** Keycloak (OAuth2 / OpenID Connect Resource Server, RBAC)
* **Database & Migrations:** PostgreSQL 16, Flyway Migrations, Spring Data JPA / Hibernate
* **Asynchronous Messaging:** RabbitMQ (Event-driven architecture with Dead Letter Exchanges & retry policies)
* **Object Storage:** MinIO (S3-compatible storage for user avatars and product assets)
* **Observability & Metrics:** Spring Boot Actuator, Prometheus metrics, SLF4J / Logback with MDC correlation IDs
* **Containerization & Orchestration:** Docker, Docker Compose, Testcontainers

---

## System Architecture & Ports

| Service / Component | Container / App Port | Actuator / Management | Description                                                 |
|:--------------------|:--------------------:|:---------------------:|:------------------------------------------------------------|
| **API Gateway**     |        `8081`        |        `9001`         | Main entry point for all API requests                       |
| **User Service**    |        `8082`        |        `9100`         | User profiles, authentication sync, avatar management       |
| **Product Service** |        `8083`        |        `9101`         | Product catalog, categories, and SKU inventory              |
| **Keycloak**        |        `8080`        |        `9000`         | IAM provider (Admin Console: `http://localhost:8080/admin`) |
| **PostgreSQL**      |        `5432`        |           —           | Primary relational database (`shopizer`)                    |
| **pgAdmin 4**       |        `5050`        |           —           | Web UI for PostgreSQL (`http://localhost:5050`)             |
| **RabbitMQ**        |        `5672`        |        `15672`        | Message broker & management UI (`http://localhost:15672`)   |
| **MinIO**           |        `9000`        |        `9001`         | S3 API (`9000`) and MinIO Console (`9001`)                  |

---

## API Gateway Routing

All external client requests flow through the **API Gateway** (`http://localhost:8081`):

| Method  | Gateway Endpoint                     | Target Service                          | Notes / Roles                                   |
|:--------|:-------------------------------------|:----------------------------------------|:------------------------------------------------|
| `POST`  | `/api/access-token`                  | Keycloak (`/realms/shopizer/.../token`) | Exchange credentials for JWT access token       |
| `GET`   | `/api/v1/users/customers/me/profile` | User Service (`8082`)                   | Requires `ROLE_CUSTOMER`                        |
| `PATCH` | `/api/v1/users/customers/me/profile` | User Service (`8082`)                   | Requires `ROLE_CUSTOMER` (application/json)     |
| `GET`   | `/api/v1/users/employees/me/profile` | User Service (`8082`)                   | Requires Store Manager / Admin / Staff role     |
| `PATCH` | `/api/v1/users/employees/me/profile` | User Service (`8082`)                   | Requires Store Manager / Admin / Staff role     |
| `PATCH` | `/api/v1/users/me/avatar`            | User Service (`8082`)                   | Requires authentication (`multipart/form-data`) |
| `*`     | `/api/v1/products/**`                | Product Service (`8083`)                | Product catalog & inventory                     |

---

## User Roles & Permissions

The system incorporates Role-Based Access Control (RBAC) managed via Keycloak:

* **Super Admin**
    * **System Control:** Highest level of authority. Manages system configurations and Keycloak integrations.
    * **User Management:** Adds, updates, or removes administrative accounts.
    * **Analytics:** Views global revenue and system health reports.
* **Store Manager**
    * **Catalog & Sales:** Manages product categories, sets up promotions/discounts, and approves orders.
    * **Inventory:** Oversees stock levels across branches.
* **Support Staff**
    * **Order Fulfillment:** Views and updates order status and processes returns/refunds.
    * **Customer Service:** Accesses customer account details for support queries.
* **Warehouse Staff**
    * **Stock Management:** Updates inventory counts, processes incoming shipments, and generates shipping labels.
* **Customer**
    * **Storefront Access:** Browses catalog, places orders, manages shipping addresses, profile, and avatar.

---

## Project Structure

```
shopizer-system/
├── shopizer-api-gateway/       # Spring Cloud Gateway routing service
├── shopizer-user-service/      # User management, customer/employee profiles, avatars
├── shopizer-product-service/   # Catalog, product categories, pricing, inventory
├── shopizer-common/            # Shared DTOs, domain models, exception handlers, utilities
├── infra/
├── docs/                       # Architecture diagrams & changelog
├── docker-compose.yml          # Core infrastructure services
```

---

## Prerequisites

Before running the project locally or in staging, ensure you have:

* **Java 21** (JDK 21+)
* **Apache Maven 3.8+**
* **Docker** & **Docker Compose**
* **MinIO Client (`mc`)** (Optional, for manual storage configuration)
* **Make** (Optional, for using convenience commands)

---

## Getting Started

### 1. Spin up all services using Docker Compose:

```bash
# Using Makefile
make up

# Or using Docker Compose directly
docker compose up -d
```

### 2. Set up keycloak client and email credentials manually.

Go to: keycloak -> clients -> user-service client -> credentials -> regenerate the access-secret and copy it to the .env
file as KEYCLOAK_USER_SERVICE_CLIENT_SECRET
Then restart docker to apply the new secret.

### 3. Initialize required S3 buckets and access policies:

```bash
bash ./infra/minio/setup.sh
```

### 4. Firewall:

```bash
sudo ufw allow 8080/tcp  # Keycloak
sudo ufw allow 8081/tcp  # API Gateway
sudo ufw allow 8082/tcp  # User Service
sudo ufw allow 9000/tcp  # MinIO API
```