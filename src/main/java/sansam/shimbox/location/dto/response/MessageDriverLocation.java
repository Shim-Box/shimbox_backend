package sansam.shimbox.location.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageDriverLocation(
        @JsonProperty("type") String type,
        @JsonProperty("payload") Payload payload
) {
    public record Payload(
            @JsonProperty("userId") String userId,
            @JsonProperty("region") String region,
            @JsonProperty("lat") double lat,
            @JsonProperty("lng") double lng,
            @JsonProperty("captured_at") String capturedAt,
            @JsonProperty("address_short") String addressShort
    ) {}
}
