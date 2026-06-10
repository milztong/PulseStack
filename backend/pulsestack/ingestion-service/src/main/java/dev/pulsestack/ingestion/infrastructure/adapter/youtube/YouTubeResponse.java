package dev.pulsestack.ingestion.infrastructure.adapter.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouTubeResponse(List<YouTubeItem> items) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record YouTubeItem(YouTubeId id, YouTubeSnippet snippet) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record YouTubeId(@JsonProperty("videoId") String videoId) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record YouTubeSnippet(
        String title,
        String channelTitle,
        String publishedAt,
        YouTubeThumbnails thumbnails
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record YouTubeThumbnails(YouTubeThumbnail medium) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record YouTubeThumbnail(String url) {}
