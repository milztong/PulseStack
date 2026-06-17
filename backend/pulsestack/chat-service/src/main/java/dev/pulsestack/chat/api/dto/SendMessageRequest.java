package dev.pulsestack.chat.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SendMessageRequest(
        @NotNull UUID channelId,
        @NotBlank @Size(min = 1, max = 4000) String content
) {}
