# PulseStack

> Real-time workspace combining human chat with automated trends from Reddit, YouTube, GitHub, and NewsAPI.

## Architecture

Event-driven microservices built with Java 21 / Spring Boot 3 and Apache Kafka.

```
External APIs → Ingestion Service → Kafka (raw-news) → Processing Service → WebSocket → React Frontend
                      ↓                                        ↓
                   Redis (dedup)                         PostgreSQL (history)
```

## Quick Start

**Prerequisites:** Java 21, Maven, Docker

```bash
# 1. Infrastructure starten
docker compose up -d

# 2. Projekt bauen
mvn clean install

# 3. Services starten (jeweils in eigenem Terminal)
cd ingestion-service  && mvn spring-boot:run
cd processing-service && mvn spring-boot:run
cd chat-service       && mvn spring-boot:run
```

**Kafka UI:** http://localhost:8090  
**Ingestion Health:** http://localhost:8081/actuator/health  
**Chat Health:** http://localhost:8082/actuator/health  
**Processing Health:** http://localhost:8083/actuator/health

## Module

| Module | Port | Responsibility |
|--------|------|----------------|
| `shared-domain` | – | Domain models (NewsItem, Channel) – no Spring |
| `ingestion-service` | 8081 | Polls APIs, deduplicates via Redis, publishes to Kafka |
| `processing-service` | 8083 | Consumes Kafka events, persists to PostgreSQL, broadcasts via WebSocket |
| `chat-service` | 8082 | Real-time chat, presence tracking via Redis |

## Tech Stack

Java 21 · Spring Boot 3.3 · Apache Kafka · PostgreSQL 16 · Redis 7 · WebSocket (STOMP) · Flyway · Testcontainers · GitHub Actions

## Design Decisions

See [docs/adr/](docs/adr/) for Architecture Decision Records.
