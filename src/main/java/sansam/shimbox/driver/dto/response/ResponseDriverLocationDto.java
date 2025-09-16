package sansam.shimbox.driver.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDriverLocationDto {

    @Schema(description = "시뮬레이션 드라이버 ID", example = "sim-1")
    private String driverId;

    @Schema(description = "위도", example = "37.5665")
    private double latitude;

    @Schema(description = "경도", example = "126.9780")
    private double longitude;

    @Schema(description = "방향(도)", example = "135.0")
    private double heading;

    @Schema(description = "속도(m/s)", example = "3.2")
    private double speed;

    @Schema(description = "갱신시각")
    private LocalDateTime updatedAt;
}




