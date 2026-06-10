package dev.pulsestack.ingestion.infrastructure.adapter.reddit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RedditResponse(RedditData data) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record RedditData(List<RedditChild> children) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record RedditChild(RedditPost data) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record RedditPost(
        String id,
        String title,
        String author,
        String permalink,
        Integer score,
        @JsonProperty("created_utc") Double createdUtc
) {}
