# E-Commerce Shopizer

## Overview

**Shopizer** is a microservice-based e-commerce backend platform designed specifically for selling computer components.
Built with Java 21 and Spring Boot, it is engineered to be a production-ready system equipped for high availability,
load balancing, and stress testing.

---

## User Roles & Permissions

The system incorporates Role-Based Access Control (RBAC) managed via Keycloak:

* **Super Admin**
    * **System Control:** Highest level of authority. Manages system configurations, payment settings, and Keycloak
      integrations.
    * **User Management:** Adds, updates, or removes other Admin users.
    * **Analytics:** Views total platform revenue and global system reports.

* **Store Manager**
    * **Catalog & Sales:** Manages product categories, sets up promotions/discounts, and approves orders.
    * **Inventory:** Oversees stock levels (without access to manage administrative staff).

* **Support Staff**
    * **Order Fulfillment:** Views and updates order status (e.g., *Confirmed*, *Delivered*, etc.) and processes
      returns/refunds.
    * **Customer Service:** Accesses customer account details for support queries.

* **Warehouse Staff**
    * **Stock Management:** Updates inventory counts, processes incoming goods, and prints shipping labels.

* **Customer**
    * **Storefront Access:** Browses products, places orders, manages personal shipping addresses, and tracks order
      history.

---

## Project Structure

The repository is organized into distinct microservices and infrastructure components:

| Module / Directory         | Description                                                     |
|:---------------------------|:----------------------------------------------------------------|
| `shopizer-api-gateway`     | Spring Cloud Gateway entry point for request routing            |
| `shopizer-user-service`    | User profile and authentication management                      |
| `shopizer-product-service` | Product catalog, categories, and SKU management                 |
| `shopizer-common`          | Shared utilities, DTOs, and domain models                       |
| `shopizer-rabbitmq`        | Messaging integration layer and retry policies                  |
| `infra`                    | Infrastructure configurations, including Keycloak realm exports |
| `docs`                     | Project documentation                                           |

---

## Architecture & Tech Stack

* **Language & Framework:** Java 21, Spring Boot, Maven
* **API Gateway:** Spring Cloud Gateway
* **Security & Identity:** Keycloak (OAuth2 / OpenID Connect)
* **Messaging & Events:** RabbitMQ / Apache Kafka
* **Monitoring & Observability:** Prometheus, Jaeger (Distributed Tracing), Elasticsearch
* **Containerization:** Docker & Docker Compose

---

## Prerequisites

Ensure you have the following installed on your environment before running the project:

* **Java 21** or higher
* **Docker** & **Docker Compose**
* **Make** (Optional, for using the `Makefile` commands)

---

## Getting Started

### 1. Build and Run via Docker Compose

```bash
# bring all services up. It may take 15 minutes to build and run docker images
docker compose up -d  
```