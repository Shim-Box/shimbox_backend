package sansam.shimbox.location.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageDriverHealth(
        @JsonProperty("type") String type,
        @JsonProperty("payload") Payload payload
) {
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Payload(
            @JsonProperty("userId") String userId,
            @JsonProperty("step") Integer step,
            @JsonProperty("heartRate") Integer heartRate,
            @JsonProperty("captured_at") String capturedAt,
            @JsonProperty("score") Double score,
            @JsonProperty("level") String level,
            @JsonProperty("isFallDetected") Boolean isFallDetected
    ) {}
}
