package sansam.shimbox.driver.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import sansam.shimbox.driver.domain.Driver;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseWeeklyWorkStatsDto {

    @Schema(description = "기사 ID", example = "1")
    private Long driverId;

    @Schema(description = "기사명", example = "김기사")
    private String driverName;

    @Schema(description = "일별 근무 통계 목록")
    private List<DailyWorkStats> dailyStats;

    @Schema(description = "총 근무 시간", example = "2400")
    private Long totalWorkMinutes;

    @Schema(description = "총 배달 건수", example = "45")
    private Integer totalDeliveryCount;

    @Schema(description = "평균 근무 시간", example = "342")
    private Double averageDailyWorkMinutes;

    @Schema(description = "평균 배달 건수", example = "6.4")
    private Double averageDailyDeliveryCount;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyWorkStats {
        @Schema(description = "날짜", example = "2025-01-13")
        private String date;

        @Schema(description = "일별 근무 시간", example = "480")
        private Long workMinutes;

        @Schema(description = "일별 배달 건수", example = "8")
        private Integer deliveryCount;
    }

    public static ResponseWeeklyWorkStatsDto from(Driver driver,
                                                List<DailyWorkStats> dailyStats, long totalWorkMinutes,
                                                int totalDeliveryCount, double averageDailyWorkMinutes,
                                                double averageDailyDeliveryCount) {
        return ResponseWeeklyWorkStatsDto.builder()
                .driverId(driver.getDriverId())
                .driverName(driver.getUser().getName())
                .dailyStats(dailyStats)
                .totalWorkMinutes(totalWorkMinutes)
                .totalDeliveryCount(totalDeliveryCount)
                .averageDailyWorkMinutes(averageDailyWorkMinutes)
                .averageDailyDeliveryCount(averageDailyDeliveryCount)
                .build();
    }
}
