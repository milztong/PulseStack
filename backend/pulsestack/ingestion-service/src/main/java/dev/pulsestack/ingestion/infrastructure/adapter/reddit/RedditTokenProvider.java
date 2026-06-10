package dev.pulsestack.ingestion.infrastructure.adapter.reddit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Holt OAuth2 Access Token von Reddit via Client Credentials Flow.
 * Token wird gecacht bis er abläuft.
 */
@Component
public class RedditTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(RedditTokenProvider.class);

    private final WebClient webClient;
    private final String clientId;
    private final String clientSecret;

    private String cachedToken;
    private long tokenExpiresAt;

    public RedditTokenProvider(
            WebClient.Builder webClientBuilder,
            @Value("${pulsestack.reddit.client-id:stub-client-id}") String clientId,
            @Value("${pulsestack.reddit.client-secret:stub-client-secret}") String clientSecret
    ) {
        this.webClient = webClientBuilder
                .baseUrl("https://www.reddit.com")
                .defaultHeader("User-Agent", "PulseStack/1.0")
                .build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiresAt) {
            return cachedToken;
        }
        return fetchNewToken();
    }

    private String fetchNewToken() {
        // Stub-Modus: kein echter API-Call wenn keine echten Credentials
        if ("stub-client-id".equals(clientId)) {
            log.debug("Reddit stub mode: returning fake token");
            cachedToken = "stub-token";
            tokenExpiresAt = System.currentTimeMillis() + 3600_000;
            return cachedToken;
        }

        log.info("Fetching new Reddit access token");
        Map response = webClient.post()
                .uri("/api/v1/access_token")
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        cachedToken = String.valueOf(response.get("access_token"));
        int expiresIn = Integer.parseInt(String.valueOf(response.get("expires_in")));
        tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000L);

        log.info("Reddit token fetched, expires in {}s", expiresIn);
        return cachedToken;
    }
}
