package dev.pulsestack.ingestion.infrastructure.adapter.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record GitHubSearchResponse(
        @JsonProperty("total_count") int totalCount,
        List<GitHubRepo> items
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record GitHubRepo(
        Long id,
        @JsonProperty("full_name") String fullName,
        String description,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("stargazers_count") Integer stargazersCount,
        @JsonProperty("owner") GitHubOwner owner,
        @JsonProperty("created_at") String createdAt
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record GitHubOwner(@JsonProperty("login") String login) {}
