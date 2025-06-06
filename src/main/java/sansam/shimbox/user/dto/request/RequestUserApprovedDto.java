package sansam.shimbox.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.global.common.RequestPagingDto;

@Getter
public class RequestUserApprovedDto extends RequestPagingDto {

    @Schema(description = "근무지", example = "구로구")
    private String residence;

    @Schema(description = "근무 상태", example = "출근")
    private Attendance attendance;

    @Schema(description = "건강 상태", example = "좋음")
    private ConditionStatus conditionStatus;
}