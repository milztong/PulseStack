package dev.pulsestack.ingestion.domain.port;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.domain.model.NewsItem;

import java.util.List;

/**
 * Eingehender Port: Jede API-Quelle implementiert dieses Interface.
 *
 * Open/Closed Prinzip: Neue Quelle = neue Klasse, kein if-else.
 * Spring injiziert alle Implementierungen automatisch als List<NewsSourcePort>.
 *
 * Contract: Gibt niemals null zurueck. Bei Fehler: leere Liste + Log.
 */
public interface NewsSourcePort {

    /**
     * Holt aktuelle News fuer den gegebenen Channel.
     *
     * @param channel Der Ziel-Channel (z.B. "java", "ai")
     * @return Nie null – leere Liste wenn keine Daten verfuegbar
     */
    List<NewsItem> fetchLatest(Channel channel);

    /**
     * Wird von Spring genutzt um den richtigen Adapter zu identifizieren.
     * Jeder Adapter gibt seine NewsSource zurueck.
     */
    dev.pulsestack.domain.model.NewsSource getSource();
}
