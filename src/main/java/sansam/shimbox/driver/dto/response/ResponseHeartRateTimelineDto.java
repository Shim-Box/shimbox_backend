package sansam.shimbox.driver.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseHeartRateTimelineDto {

    @Schema(description = "기사 사용자 ID", example = "123")
    private Long userId;

    @Schema(description = "기사명", example = "김기사")
    private String driverName;

    @Schema(description = "심박수", example = "80")
    private Integer heartRate;

    @Schema(description = "기록 시간", example = "2025-01-20T10:30:00")
    private LocalDateTime recordedAt;

    public static ResponseHeartRateTimelineDto from(Long userId, String driverName, Integer heartRate, LocalDateTime recordedAt) {
        return ResponseHeartRateTimelineDto.builder()
                .userId(userId)
                .driverName(driverName)
                .heartRate(heartRate)
                .recordedAt(recordedAt)
                .build();
    }
}
