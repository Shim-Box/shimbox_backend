package sansam.shimbox.driver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sansam.shimbox.driver.enums.Attendance;

@Getter
@NoArgsConstructor
public class RequestUpdateAttendanceDto {

    @Schema(description = "변경할 근무 상태", example = "출근")
    private Attendance status;
}
