package sansam.shimbox.driver.dto.response;

import lombok.Builder;
import lombok.Getter;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.driver.enums.Finish1;
import sansam.shimbox.driver.enums.Finish2;
import sansam.shimbox.driver.enums.Finish3;


import java.time.LocalDateTime;

@Getter
@Builder
public class ResponseLeaveWorkDto {
    private LocalDateTime workTime;
    private LocalDateTime leaveWorkTime;
    private Finish1 finish1;
    private Finish2 finish2;
    private Finish3 finish3;
    private Integer step;
    private Integer heartRate;
    private ConditionStatus conditionStatus;
}
