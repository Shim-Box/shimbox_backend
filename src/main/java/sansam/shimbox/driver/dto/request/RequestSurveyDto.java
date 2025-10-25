package sansam.shimbox.driver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.driver.enums.Finish1;
import sansam.shimbox.driver.enums.Finish2;
import sansam.shimbox.driver.enums.Finish3;

@Getter
@AllArgsConstructor
public class RequestSurveyDto {

    @Schema(description = "첫번째 설문", example = "비슷했다")
    private final Finish1 finish1;

    @Schema(description = "두번째 설문", example = "약간 그렇다")
    private final Finish2 finish2;

    @Schema(description = "세번째 설문", example = "평소대로")
    private final Finish3 finish3;

    @Schema(description = "걸음수", example = "0")
    private final Integer step;

    @Schema(description = "심박수", example = "0")
    private final Integer heartRate;

    @Schema(description = "건강 상태", example = "좋음")
    private final ConditionStatus conditionStatus;
}

