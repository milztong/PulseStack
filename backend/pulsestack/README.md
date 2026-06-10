# PulseStack

> Ein hochskalierbarer Echtzeit-Workspace, der menschliche Kommunikation (Chat) mit automatisierten Trends aus **Reddit, YouTube, GitHub und NewsAPI** bündelt — gelöst trotz strenger API-Limits durch Caching, Event-Driven Architecture und 25 geteilte Topic-Channels.

[![CI](https://github.com/DEIN_USERNAME/pulsestack/actions/workflows/ci.yml/badge.svg)](https://github.com/DEIN_USERNAME/pulsestack/actions/workflows/ci.yml)
[![Coverage](https://raw.githubusercontent.com/DEIN_USERNAME/pulsestack/main/.github/badges/jacoco.svg)](https://github.com/DEIN_USERNAME/pulsestack/actions/workflows/ci.yml)
[![Branches](https://raw.githubusercontent.com/DEIN_USERNAME/pulsestack/main/.github/badges/branches.svg)](https://github.com/DEIN_USERNAME/pulsestack/actions/workflows/ci.yml)

---

## Architektur

```
┌─────────────────────────────────────────────────────────────────────┐
│                         EXTERNAL APIs                               │
│   Reddit OAuth2 · YouTube Data v3 · GitHub REST · NewsAPI          │
└────────────────────────┬────────────────────────────────────────────┘
                         │ HTTP (WebClient)
                         ▼
┌────────────────────────────────────────┐
│         Ingestion Service :8081        │  – pollt alle 5h
│  ┌──────────────┐  ┌────────────────┐  │  – 4 Adapter (Open/Closed)
│  │ NewsSource   │  │ Redis Dedup    │  │  – Redis verhindert Duplikate
│  │ Port x4      │  │ SETEX 24h TTL  │  │
│  └──────┬───────┘  └────────────────┘  │
└─────────┼──────────────────────────────┘
          │ Kafka Topic: raw-news
          ▼
┌────────────────────────────────────────┐
│        Processing Service :8083        │  – konsumiert Kafka
│  ┌──────────────┐  ┌────────────────┐  │  – speichert in PostgreSQL
│  │  Kafka       │  │  WebSocket     │  │  – broadcastet via STOMP
│  │  Consumer    │→ │  Broadcaster   │  │  – REST: News + Analytics + Search
│  └──────────────┘  └────────┬───────┘  │
└───────────────────────────  │  ────────┘
          │ PostgreSQL        │ WebSocket /topic/channel/{id}
          ▼                   ▼
┌──────────────┐    ┌─────────────────────────────────┐
│  Chat        │    │       React Frontend :5173       │
│  Service     │───▶│  Sidebar (25 Channels)          │
│  :8082       │    │  Live News Feed (STOMP)          │
│  Redis       │    │  Chat + Typing-Indicator        │
│  Presence    │    │  Analytics Dashboard (Recharts) │
└──────────────┘    │  Cross-Platform Suche           │
                    └─────────────────────────────────┘
          ▲
┌─────────┴────────┐
│  Auth Service    │  JWT-Signing  (HS256)
│  :8084           │  Register / Login
│  BCrypt +        │  Token gültig 24h
│  JJWT 0.12       │
└──────────────────┘
```

---

## Setup in 3 Schritten

**Voraussetzungen:** Java 21, Maven 3.8+, Docker Desktop

```bash
# 1. Infrastruktur starten (PostgreSQL, Redis, Kafka, Kafka UI)
cd backend/pulsestack
docker compose up -d

# 2. API-Keys konfigurieren
cp .env.example .env
# .env öffnen und Werte eintragen (Reddit, YouTube, GitHub, NewsAPI)

# 3. Alle Services starten
mvn spring-boot:run -pl ingestion-service  &
mvn spring-boot:run -pl processing-service &
mvn spring-boot:run -pl chat-service       &
mvn spring-boot:run -pl auth-service       &

# 4. Frontend
cd ../../frontend/pulsestack-ui
npm install && npm run dev
```

Browser: **http://localhost:5173**

---

## Services & Ports

| Service | Port | Swagger UI | Verantwortung |
|---|---|---|---|
| `ingestion-service` | 8081 | [/swagger-ui.html](http://localhost:8081/swagger-ui.html) | API-Polling, Redis-Dedup, Kafka-Publish |
| `chat-service` | 8082 | – | Echtzeit-Chat, Presence (Redis) |
| `processing-service` | 8083 | [/swagger-ui.html](http://localhost:8083/swagger-ui.html) | Kafka-Consumer, PostgreSQL, WebSocket, Analytics, Suche |
| `auth-service` | 8084 | [/swagger-ui.html](http://localhost:8084/swagger-ui.html) | Register/Login, JWT-Ausgabe |
| Kafka UI | 8090 | [localhost:8090](http://localhost:8090) | Kafka-Events live beobachten |

---

## API-Endpunkte (Übersicht)

```
POST /api/v1/auth/register          → { token, username }
POST /api/v1/auth/login             → { token, username }

GET  /api/v1/channels               → 25 Topic-Channels
GET  /api/v1/news/channel/{id}      → News für einen Channel
GET  /api/v1/analytics/trends?days= → Trend-Daten für Recharts
GET  /api/v1/search?q=&limit=       → Cross-Platform Volltextsuche

WS   /ws  (STOMP)
  SUBSCRIBE /topic/channel/{id}     → Live News-Events
  SUBSCRIBE /topic/chat/{id}        → Live Chat-Nachrichten
  SEND /app/chat.send               → Nachricht senden
```

---

## Umgebungsvariablen

| Variable | Beschreibung | Pflicht |
|---|---|---|
| `REDDIT_CLIENT_ID` | Reddit OAuth2 App ID | Für Reddit-Quelle |
| `REDDIT_CLIENT_SECRET` | Reddit OAuth2 Secret | Für Reddit-Quelle |
| `YOUTUBE_API_KEY` | YouTube Data API v3 Key | Für YouTube-Quelle |
| `GITHUB_TOKEN` | GitHub Personal Access Token | Für GitHub-Quelle |
| `NEWSAPI_KEY` | NewsAPI.org API Key | Für NewsAPI-Quelle |
| `JWT_SECRET` | JWT Signing Secret (min. 32 Zeichen) | **Ja** |

> Services starten auch ohne API-Keys — Adapter ohne Key werden via `@ConditionalOnProperty` deaktiviert. Nur Reddit ist standardmäßig aktiv.

---

## Tech-Entscheidungen

| Problem | Lösung | Warum |
|---|---|---|
| 4 verschiedene APIs mit Rate-Limits | Shared Ingestion (1 Poll für alle User) | Quota geteilt statt multipliziert |
| Keine Duplikate in Kafka | Redis `SETEX` mit 24h TTL | O(1), kein DB-Overhead |
| Neue API-Quelle hinzufügen | Neue Klasse, `NewsSourcePort` implementieren | Open/Closed — kein bestehender Code geändert |
| Echtzeit ohne Polling | Kafka → WebSocket (STOMP) | Entkopplung + sofortige Zustellung |
| Presence-Status (Online/Offline) | Redis HSET + TTL | Automatisches Expiry ohne Cron-Job |
| Testbarkeit ohne echte APIs | Ports & Adapters (Hexagonal) | Service mit Mockito vollständig testbar |

Ausführliche Begründungen: [docs/adr/](docs/adr/)

---

## Architektur-Prinzipien

- **Hexagonale Architektur** — `NewsIngestionService` kennt nur Ports, niemals Adapter
- **Event-Driven** — Kafka entkoppelt Ingestion vollständig von Processing
- **Single Responsibility** — jeder Service hat genau einen Änderungsgrund
- **Constructor Injection** — kein `@Autowired` auf Feldern
- **Record Types** — immutable Domain-Modelle ohne Getter-Boilerplate
- **Graceful Degradation** — Adapter-Fehler stoppen nie den Gesamt-Job

---

## Modul-Struktur

```
pulsestack/
├── shared-domain/          Pure Java — Records: NewsItem, Channel, NewsSource
├── ingestion-service/      Port 8081 — API-Adapter, Redis-Dedup, Kafka-Producer
├── processing-service/     Port 8083 — Kafka-Consumer, JPA, WebSocket, REST
├── chat-service/           Port 8082 — Chat, Presence
├── auth-service/           Port 8084 — JWT Register/Login
├── docs/adr/               Architecture Decision Records
└── docker-compose.yml      PostgreSQL · Redis · Kafka · Kafka UI
```
