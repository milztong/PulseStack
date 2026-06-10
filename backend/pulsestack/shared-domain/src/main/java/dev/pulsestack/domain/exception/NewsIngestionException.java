package dev.pulsestack.domain.exception;

public class NewsIngestionException extends RuntimeException {

    public NewsIngestionException(String message) {
        super(message);
    }

    public NewsIngestionException(String message, Throwable cause) {
        super(message, cause);
    }
}
