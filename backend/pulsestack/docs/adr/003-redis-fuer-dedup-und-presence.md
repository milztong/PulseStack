# ADR 003 – Redis für Deduplizierung und Presence-System

**Datum:** 2024-12  
**Status:** Accepted

---

## Kontext

PulseStack steht vor zwei unterschiedlichen Problemen, die beide ein schnelles In-Memory-System erfordern:

1. **Deduplizierung:** Der Ingestion-Service pollt jede API alle 5 Stunden. Dasselbe Reddit-Post oder derselbe GitHub-Repo würde bei jedem Lauf erneut in Kafka landen, wenn wir nicht prüfen, ob wir ihn schon gesehen haben.

2. **Presence-System:** Der Chat-Service muss wissen, welche User gerade online sind. Dieser Status ist flüchtig — nach einem Browser-Schließen soll der User nach 60 Sekunden als offline gelten.

---

## Alternativen bewertet

| Option | Deduplizierung | Presence | Problem |
|---|---|---|---|
| PostgreSQL `UNIQUE`-Constraint | ✅ | ❌ | Presence braucht TTL + Polling; zu langsam |
| PostgreSQL für beides | ✅ | ⚠️ | Presence-Queries würden die DB belasten; kein natürliches Expiry |
| Redis für beides | ✅ | ✅ | Einzige Schwäche: weiterer Service |
| Nur In-Memory (ConcurrentHashMap) | ⚠️ | ⚠️ | Geht verloren bei Service-Neustart; nicht cluster-fähig |

---

## Entscheidung

Wir verwenden **Redis für beide Anwendungsfälle:**

**Deduplizierung:**
```
SETEX dedup:{externalId} 86400 "1"   # TTL = 24h
```
Vor jedem Kafka-Publish wird geprüft, ob die ID bereits existiert. Durch den TTL werden alte IDs automatisch gelöscht — kein Cleanup-Job nötig.

**Presence:**
```
HSET presence:channel:{channelId} {username} {timestamp}
EXPIRE presence:channel:{channelId} 60
```
Der Client sendet alle 30s einen Heartbeat. Nach 60s ohne Heartbeat läuft der Key aus — der User gilt als offline. Kein expliziter "Logout" nötig.

---

## Konsequenzen

**Positiv:**
- Deduplizierung: O(1) pro Check — skaliert auf Millionen von Events
- Presence: Kein Polling, kein Cron-Job — Redis TTL erledigt das Cleanup von selbst
- Beide Anwendungsfälle teilen dieselbe Redis-Instanz → weniger Infrastruktur
- Redis ist bereits im Stack (Docker Compose) — keine neue Abhängigkeit

**Negativ:**
- Redis ist ein weiterer laufender Service (Komplexität beim lokalen Setup)
- Bei Redis-Ausfall: Ingestion akzeptiert ggf. Duplikate (graceful degradation), Presence-System fällt aus
- Persistenz: Redis-Data geht bei Neustart verloren — für Deduplizierung und Presence ist das akzeptabel, für andere Use-Cases nicht

**Mitigation für Ausfall:** Beide Systeme sind so implementiert, dass ein Redis-Ausfall den Hauptfluss (Kafka, Chat) nicht blockiert. Im Fehlerfall wird geloggt und weitergemacht.
