package sansam.shimbox.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sansam.shimbox.driver.enums.Region;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDriverRegionDto {

    @Schema(description = "기사 ID", example = "1")
    private Long driverId;

    @Schema(description = "기사 이름", example = "이준영")
    private String driverName;

    @Schema(description = "배정된 지역 1", example = "구로구")
    private Region region1;

    @Schema(description = "배정된 지역 2", example = "양천구")
    private Region region2;
}
