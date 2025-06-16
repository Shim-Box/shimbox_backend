package sansam.shimbox.driver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import sansam.shimbox.driver.enums.ConditionStatus;

@Getter
@Setter
public class RequestRealTimeHealthSaveDto {

    @Schema(description = "걸음수", example = "5000")
    private Integer step;

    @Schema(description = "심박수", example = "80")
    private Integer heartRate;

    @Schema(description = "건강 상태", example = "좋음")
    private ConditionStatus conditionStatus;
}
