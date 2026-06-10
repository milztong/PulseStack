# ADR 002 – Hexagonale Architektur (Ports & Adapters)

**Datum:** 2024-12  
**Status:** Accepted

---

## Kontext

PulseStack muss mit vier externen APIs (Reddit, YouTube, GitHub, NewsAPI) kommunizieren, die sich in Authentifizierung, Rate-Limits und Response-Format stark unterscheiden. Gleichzeitig soll das System testbar sein, ohne echte API-Calls zu machen.

Das naive Vorgehen — direktes Aufrufen von HTTP-Clients im Service — würde bedeuten:
- Unit-Tests benötigen entweder Netzwerk oder komplexes Mocking von `WebClient`
- Eine neue Quelle erfordert Änderungen im `NewsIngestionService`
- PostgreSQL, Redis und Kafka sind direkt mit der Geschäftslogik verwoben

---

## Entscheidung

Wir verwenden die **Hexagonale Architektur** (auch: Ports & Adapters Pattern):

```
[ Externe Welt ]          [ Anwendungskern ]          [ Externe Welt ]
                                                        
Reddit API  ─────────→  NewsSourcePort  ←────────  NewsIngestionService
YouTube API ─────────→  NewsSourcePort  
GitHub API  ─────────→  NewsSourcePort  
                                                        
                     DuplicateChecker  ←────────  NewsIngestionService
Redis ───────────────→  RedisDuplicateChecker
                                                        
                     NewsEventPublisher ←────────  NewsIngestionService
Kafka ───────────────→  KafkaNewsEventPublisher
```

**Ports** = Java Interfaces im `domain.port`-Package (kein Spring, kein Framework).  
**Adapters** = Konkrete Implementierungen im `infrastructure`-Package.  
**Service** = Kennt nur Ports. Niemals Adapter-Klassen direkt.

---

## Konsequenzen

**Positiv:**
- `NewsIngestionService` ist mit reinen Mockito-Mocks vollständig testbar — kein WireMock, kein Testcontainer nötig
- Open/Closed: Eine neue Quelle ist nur eine neue Klasse, die `NewsSourcePort` implementiert. Spring registriert sie automatisch als `List<NewsSourcePort>`
- Der Kern kompiliert ohne Spring-Abhängigkeiten (`shared-domain` ist reines Java)
- Adapter können unabhängig ausgetauscht werden (z.B. Redis → Hazelcast für `DuplicateChecker`)

**Negativ:**
- Mehr Dateien und Abstraktionsschichten als ein einfacher Service
- Onboarding-Kurve für Entwickler, die das Muster nicht kennen
- Bei einfachen CRUD-Anwendungen wäre dies Overengineering

**Bewertung:** Für PulseStack mit seinen vier wechselbaren Datenquellen ist der Aufwand gerechtfertigt. Das Pattern wird bei jedem neuen Adapter sofort sichtbar belohnt.
