package sansam.shimbox.driver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sansam.shimbox.driver.enums.Attendance;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ResponseAttendanceDto {
    private Attendance attendance;
    private LocalDateTime timestamp;
}