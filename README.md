# WebhookDelivery

A production-grade webhook delivery engine built with Spring Boot. Inspired by how Stripe and GitHub reliably deliver HTTP events to third-party systems.

[![CI](https://github.com/OzanCikriklioglu-cloud/webhook-delivery/actions/workflows/ci.yml/badge.svg)](https://github.com/OzanCikriklioglu-cloud/webhook-delivery/actions/workflows/ci.yml)

---

## What problem does it solve?

When a system sends an HTTP event to another service, the target might be down, slow, or returning errors. Without a reliable delivery mechanism, that event is lost forever.

WebhookDelivery solves this by:
- Queuing every event in RabbitMQ before attempting delivery
- Retrying failed deliveries with exponential backoff (1 min → 5 min → 30 min)
- Moving exhausted events to a dead-letter queue for inspection
- Logging every attempt with status code, duration, and error message

---

## Architecture

```
Client
  │
  │  POST /api/events
  ▼
┌─────────────────┐
│   Spring Boot   │  ── saves event to PostgreSQL
│   REST API      │  ── publishes to RabbitMQ
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│              RabbitMQ                   │
│                                         │
│  webhook.delivery.queue                 │
│  webhook.retry.1m.queue  (TTL: 1 min)  │
│  webhook.retry.5m.queue  (TTL: 5 min)  │
│  webhook.retry.30m.queue (TTL: 30 min) │
│  webhook.dlq.queue       (dead letter) │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────┐
│ DeliveryWorker  │  ── makes HTTP POST to target URL
│ (Consumer)      │  ── logs result to PostgreSQL
└────────┬────────┘
         │
    success? ──► DELIVERED ──► done
         │
    failure? ──► retry queue (backoff)
         │
  max retries? ──► DLQ ──► FAILED
```

---

## How it works

1. A client registers a target URL as a **webhook endpoint**
2. Client fires an **event** via REST API with a payload
3. Event is saved to the database and published to RabbitMQ
4. **DeliveryWorker** picks it up and makes an HTTP POST to the target URL
5. On success → event marked `COMPLETED`, delivery log saved
6. On failure → event goes to the appropriate retry queue (1m, 5m, or 30m delay)
7. After max retries → event moves to the dead-letter queue, marked `FAILED`
8. A **RetryScheduler** runs every minute to recover any events stuck in the pipeline
9. Every attempt is logged with status, HTTP code, duration, and error message

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Messaging | RabbitMQ 3 |
| HTTP Client | Spring WebFlux (WebClient) |
| Security | Spring Security + JWT |
| Containerization | Docker + Docker Compose |
| CI | GitHub Actions |

---

## Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running
- That's it.

### Setup

**1. Clone the repository**
```bash
git clone https://github.com/OzanCikriklioglu-cloud/webhook-delivery.git
cd webhook-delivery
```

**2. Create your `.env` file**
```bash
cp .env.example .env
```

Edit `.env` with your values:
```
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_secret_key_at_least_32_characters_long
```

**3. Start everything**
```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080`.

### First-time reset (if needed)
```bash
docker-compose down -v
docker-compose up --build
```

---

## API Endpoints

All endpoints except `/api/auth/**` require a `Bearer` JWT token in the `Authorization` header.

### Auth

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/endpoints` | Register a webhook endpoint |
| GET | `/api/endpoints` | List your endpoints |
| GET | `/api/endpoints/{id}` | Get a specific endpoint |
| PUT | `/api/endpoints/{id}` | Update an endpoint |
| DELETE | `/api/endpoints/{id}` | Delete an endpoint |
| GET | `/api/endpoints/{id}/events` | List events for an endpoint |
| POST | `/api/endpoints/{id}/regenerate-secret` | Rotate the secret key |

### Events

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/events` | Fire a webhook event |
| GET | `/api/events` | List all your events |
| GET | `/api/events/{id}` | Get event status |
| GET | `/api/events/{id}/logs` | Get delivery attempt logs |
| POST | `/api/events/{id}/retry` | Manually retry a failed event |
| POST | `/api/events/{id}/cancel` | Cancel a pending event |
| GET | `/api/events/statistics` | Get event counts by status |
| GET | `/api/events/endpoint/{id}` | List events by endpoint |

---

## Example Usage

**Register and login:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

**Create an endpoint:**
```bash
curl -X POST http://localhost:8080/api/endpoints \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"My Endpoint","url":"https://example.com/webhook"}'
```

**Fire an event:**
```bash
curl -X POST http://localhost:8080/api/events \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"endpointId":1,"eventType":"payment.completed","payload":"{\"amount\":99.99}"}'
```

**Check delivery logs:**
```bash
curl http://localhost:8080/api/events/1/logs \
  -H "Authorization: Bearer <token>"
```

---

## RabbitMQ Dashboard

Available at `http://localhost:15672` (username: `user`, password: `pass`).

You will see these queues:

| Queue | Purpose |
|---|---|
| `webhook.delivery.queue` | Events waiting for first delivery |
| `webhook.retry.1m.queue` | Failed events waiting 1 minute before retry |
| `webhook.retry.5m.queue` | Failed events waiting 5 minutes before retry |
| `webhook.retry.30m.queue` | Failed events waiting 30 minutes before retry |
| `webhook.dlq.queue` | Events that exhausted all retries |

---

## Running Tests

```bash
./mvnw test
```

21 tests covering `RetryPolicy` (delay calculation, backoff logic) and `DeliveryService` (success, HTTP errors, inactive endpoint, timeout, unreachable URL).

---

## Why I built this

I built this project to understand how reliable event delivery systems work in production. Services like Stripe and GitHub solve this exact problem at scale — every webhook you receive from them goes through a system like this. This is my implementation of the same concept using Java and Spring Boot, built as part of my backend development journey.
