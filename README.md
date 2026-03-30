# in the pipe line ( not done yet! )

# WebhookDelivery

A production-grade webhook delivery engine built with Spring Boot. Inspired by how Stripe and GitHub reliably deliver events to third-party systems.

## What problem does it solve?

When a system sends an HTTP event to another service, the target might be down, slow, or returning errors. Without a reliable delivery mechanism, 
that event is lost forever. WebhookDelivery solves this by queuing every event, attempting delivery, and automatically retrying failed attempts with exponential backoff.
If all retries are exhausted, the event moves to a dead-letter queue for inspection.

## How it works

1. A client registers a target URL (webhook endpoint)
2. Client fires an event via REST API
3. Event is published to RabbitMQ queue
4. Delivery worker picks it up and makes an HTTP POST to the target URL
5. On failure, the system waits and retries (1 min, 5 min, 30 min)
6. After max retries, event moves to dead-letter queue
7. Every attempt is logged with status, response code, and timestamp

## Tech Stack

- Java 21 + Spring Boot 3
- PostgreSQL (persistent storage)
- RabbitMQ (async messaging)
- Flyway (database migrations)
- Docker Compose (one command setup)
- Spring Security + JWT (API protection)
- GitHub Actions (CI pipeline)

## Getting Started

!!!Make sure you have Docker Desktop installed and running.

The API will be available at `http://localhost:8080`.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register a new user |
| POST | /api/auth/login | Login and get JWT token |
| POST | /api/endpoints | Register a webhook endpoint |
| GET | /api/endpoints | List your endpoints |
| DELETE | /api/endpoints/{id} | Delete an endpoint |
| POST | /api/events | Fire an event |
| GET | /api/events/{id} | Get event status |
| GET | /api/events/{id}/logs | Get delivery attempt logs |

## Key Features

- Async event delivery via RabbitMQ
- Automatic retry with exponential backoff
- Dead-letter queue for failed events
- Full delivery log per attempt
- Scheduled job to recover stuck events
- JWT protected API
- CI pipeline with GitHub Actions
- One command Docker setup

## Why I built this

I built this project to understand how reliable event delivery systems work in production. Services like Stripe and GitHub solve this exact problem at scale. 
This is my implementation of the same concept using Java and Spring Boot, built as part of my backend development journey.
