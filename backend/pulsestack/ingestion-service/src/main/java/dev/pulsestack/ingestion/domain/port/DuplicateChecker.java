package dev.pulsestack.ingestion.domain.port;

public interface DuplicateChecker {

    boolean isAlreadySeen(String externalId);
}
