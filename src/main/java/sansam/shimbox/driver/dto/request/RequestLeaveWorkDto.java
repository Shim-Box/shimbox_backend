package sansam.shimbox.driver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import sansam.shimbox.driver.enums.Finish1;
import sansam.shimbox.driver.enums.Finish2;
import sansam.shimbox.driver.enums.Finish3;

@Getter
@AllArgsConstructor
public class RequestLeaveWorkDto {

    @Schema(description = "첫번째 설문", example = "적었다")
    private final Finish1 finish1;

    @Schema(description = "두번째 설문", example = "전혀 아니다")
    private final Finish2 finish2;

    @Schema(description = "상품 ID", example = "평소대로")
    private final Finish3 finish3;
}
