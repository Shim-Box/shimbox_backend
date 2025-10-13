package sansam.shimbox.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sansam.shimbox.driver.enums.Region;

@Data
public class RequestDriverRegionDto {

    @Schema(description = "기사 ID", example = "1")
    @NotNull(message = "기사 ID는 필수입니다")
    private Long driverId;

    @Schema(description = "배정할 지역 1", example = "구로구")
    @NotNull(message = "지역1은 필수입니다")
    private Region region1;

    @Schema(description = "배정할 지역 2", example = "양천구")
    @NotNull(message = "지역2는 필수입니다")
    private Region region2;
}
