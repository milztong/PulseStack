## Was wurde geändert?

<!-- Kurze Beschreibung der Änderung (1-3 Sätze). -->

## Warum?

<!-- Kontext: Problem, Feature-Request, ADR, Ticket-Nummer. -->

## Typ der Änderung

- [ ] 🐛 Bug Fix
- [ ] ✨ Neues Feature
- [ ] ♻️ Refactoring (kein funktionaler Unterschied)
- [ ] 📝 Dokumentation
- [ ] 🔧 Konfiguration / Infra
- [ ] ⚡ Performance

## Checklist

- [ ] Code kompiliert (`mvn compile`) und alle Tests sind grün (`mvn test`)
- [ ] Neue Logik ist mit Unit-Tests abgedeckt (Mockito)
- [ ] Integrationstest angepasst oder neu geschrieben (Testcontainers) — falls relevant
- [ ] Keine Secrets oder API-Keys im Code (`grep -r "apiKey\|password\|secret" src/main`)
- [ ] `application.yml` nutzt `${ENV_VAR:default}` statt Klartext-Secrets
- [ ] Breaking Change? Wenn ja: andere Services und Frontend angepasst
- [ ] ADR angelegt oder aktualisiert — falls Architektur-Entscheidung getroffen wurde

## Screenshots / Logs (optional)

<!-- Kafka UI, Swagger, Terminalausgabe — nur wenn hilfreich. -->
