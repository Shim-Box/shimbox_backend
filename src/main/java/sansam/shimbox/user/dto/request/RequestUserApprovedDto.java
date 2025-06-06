package sansam.shimbox.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.global.common.RequestPagingDto;

@Getter

public class RequestUserApprovedDto extends RequestPagingDto {

    @Schema(description = "근무지 (예: 구로구)", example = "구로구")
    private final String residence;

    @Schema(description = "근무 상태 (예: 출근전, 출근, 퇴근)", example = "출근전")
    private final Attendance attendance;

    @Schema(description = "건강 상태 (예: 좋음, 불안, 위험)", example = "좋음")
    private final ConditionStatus conditionStatus;

    public RequestUserApprovedDto(int page, int size, String residence, Attendance attendance, ConditionStatus conditionStatus) {
        super(page, size);
        this.residence = residence;
        this.attendance = attendance;
        this.conditionStatus = conditionStatus;
    }
}