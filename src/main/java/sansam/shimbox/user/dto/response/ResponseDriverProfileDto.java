package sansam.shimbox.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.enums.Region;

import java.time.Duration;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDriverProfileDto {

    @Schema(description = "기사 ID", example = "1")
    private Long driverId;

    @Schema(description = "기사명", example = "김기사")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "거주지", example = "서울")
    private String residence;

    @Schema(description = "담당 지역 목록", example = "[\"구로구\", \"양천구\"]")
    private List<String> regions;

    @Schema(description = "전체 배송 건수", example = "20")
    private Long totalDeliveryCount;

    @Schema(description = "완료된 배송 건수", example = "15")
    private Long completedDeliveryCount;

    @Schema(description = "근무 시간 (분)", example = "480")
    private Long workDurationMinutes;

    @Schema(description = "키 (cm)", example = "175")
    private Integer height;

    @Schema(description = "몸무게 (kg)", example = "70")
    private Integer weight;

    public static ResponseDriverProfileDto from(Driver driver, List<Region> assignedRegions, 
            Long totalDeliveryCount, Long completedDeliveryCount, Duration workDuration) {
        return ResponseDriverProfileDto.builder()
                .driverId(driver.getDriverId())
                .name(driver.getUser().getName())
                .phoneNumber(driver.getUser().getPhoneNumber())
                .residence(driver.getUser().getResidence())
                .regions(assignedRegions.stream()
                        .map(Region::getLabel)
                        .toList())
                .totalDeliveryCount(totalDeliveryCount)
                .completedDeliveryCount(completedDeliveryCount)
                .workDurationMinutes(workDuration != null ? workDuration.toMinutes() : 0L)
                .height(driver.getUser().getHeight())
                .weight(driver.getUser().getWeight())
                .build();
    }
}