# ADR-001: Apache Kafka als Event-Bus statt synchronem REST

**Status:** Accepted  
**Datum:** $(date +%Y-%m-%d)  
**Autor:** PulseStack Team

## Kontext

PulseStack muss Daten von 4 externen APIs (Reddit, YouTube, GitHub, NewsAPI) holen
und diese an den Processing-Service weiterleiten, der sie in PostgreSQL speichert
und per WebSocket an Clients pushed.

## Optionen

### Option A: Direkter REST-Call (Ingestion → Processing)
- Ingestion-Service ruft `/api/process` am Processing-Service auf
- **Problem:** Enge Kopplung. Wenn Processing-Service down ist, gehen Daten verloren.
- **Problem:** Kein Retry, kein Backpressure, keine Skalierung.

### Option B: Shared Database
- Beide Services schreiben/lesen in dieselbe Tabelle
- **Problem:** Verletzt Service-Isolation. Änderungen am Schema brechen beide Services.

### Option C: Apache Kafka (gewählt)
- Ingestion publisht Events in `raw-news` Topic
- Processing konsumiert asynchron, unabhängig vom Ingestion-Service
- **Vorteile:** Entkopplung, Retry-Mechanismus, Skalierbarkeit, Event-History

## Entscheidung

**Kafka** – weil die API-Limits (z.B. 100 Requests/Tag bei NewsAPI) bedeuten,
dass wir Daten als wertvolle Events behandeln müssen, die nicht verloren gehen dürfen.
Kafka garantiert Delivery und erlaubt es uns, Consumer (Processing, Analytics, etc.)
unabhängig hinzuzufügen.

## Konsequenzen

- Lokale Entwicklung braucht Kafka (via Docker Compose gelöst)
- Mehr Komplexität als REST – gerechtfertigt durch die Entkopplung
- Kafka UI (Port 8090) macht Events sichtbar für Debugging
