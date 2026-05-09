package dev.pulsestack.ingestion.domain.port;

/**
 * Ausgehender Port: Pruefen und markieren ob ein News-Item bereits gesehen wurde.
 * Implementiert durch RedisDuplicateChecker.
 */
public interface DuplicateChecker {

    /**
     * Prueft ob die gegebene externe ID bereits verarbeitet wurde.
     * Markiert sie gleichzeitig als gesehen (atomare Operation via Redis SETNX).
     *
     * @param externalId Die Quell-ID (z.B. Reddit post_id)
     * @return true wenn das Item bereits bekannt ist (Duplikat), false wenn neu
     */
    boolean isAlreadySeen(String externalId);
}
