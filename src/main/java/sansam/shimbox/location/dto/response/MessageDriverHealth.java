package sansam.shimbox.location.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageDriverHealth(
        @JsonProperty("type") String type,
        @JsonProperty("payload") Payload payload
) {
    public record Payload(
            @JsonProperty("userId") String userId,
            @JsonProperty("step") Integer step,
            @JsonProperty("heartRate") Integer heartRate,
            @JsonProperty("capturedAt") String capturedAt
    ) {}
}
