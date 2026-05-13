package dev.pulsestack.ingestion.infrastructure;

import dev.pulsestack.domain.model.Channel;
import dev.pulsestack.domain.model.NewsItem;
import dev.pulsestack.ingestion.infrastructure.adapter.reddit.RedditApiAdapter;
import dev.pulsestack.ingestion.infrastructure.adapter.reddit.RedditTokenProvider;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedditApiAdapterTest {

    private MockWebServer mockWebServer;
    private RedditApiAdapter sut;

    private static final Channel JAVA_CHANNEL = new Channel(
            UUID.randomUUID(), "java", "Java", "Java ecosystem"
    );

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        RedditTokenProvider tokenProvider = mock(RedditTokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("stub-token");

        WebClient.Builder builder = WebClient.builder();

        sut = new RedditApiAdapter(
                builder,
                mockWebServer.url("/").toString(),  // baseUrl aus MockWebServer
                tokenProvider
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("should return mapped NewsItems when Reddit returns valid response")
    void should_returnNewsItems_when_redditReturnsValidResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                    {
                      "data": {
                        "children": [
                          {
                            "data": {
                              "id": "abc123",
                              "title": "Spring Boot 3.3 Released",
                              "author": "devuser",
                              "permalink": "/r/java/comments/abc123",
                              "score": 1500,
                              "created_utc": 1700000000.0
                            }
                          }
                        ]
                      }
                    }
                """));

        List<NewsItem> result = sut.fetchLatest(JAVA_CHANNEL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).externalId()).isEqualTo("abc123");
        assertThat(result.get(0).title()).isEqualTo("Spring Boot 3.3 Released");
        assertThat(result.get(0).author()).isEqualTo("devuser");
        assertThat(result.get(0).score()).isEqualTo(1500);
        assertThat(result.get(0).url()).isEqualTo("https://reddit.com/r/java/comments/abc123");
    }

    @Test
    @DisplayName("should return empty list when Reddit returns 403")
    void should_returnEmptyList_when_redditReturns403() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        List<NewsItem> result = sut.fetchLatest(JAVA_CHANNEL);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty list when Reddit returns no children")
    void should_returnEmptyList_when_redditReturnsNoChildren() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("""
                    {"data": {"children": []}}
                """));

        List<NewsItem> result = sut.fetchLatest(JAVA_CHANNEL);

        assertThat(result).isEmpty();
    }
}
