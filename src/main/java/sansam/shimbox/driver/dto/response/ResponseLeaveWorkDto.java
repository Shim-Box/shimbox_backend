package sansam.shimbox.driver.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.driver.enums.Finish1;
import sansam.shimbox.driver.enums.Finish2;
import sansam.shimbox.driver.enums.Finish3;
import sansam.shimbox.driver.domain.Health;


import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseLeaveWorkDto {
    private LocalDateTime workTime;
    private LocalDateTime leaveWorkTime;
    private Finish1 finish1;
    private Finish2 finish2;
    private Finish3 finish3;
    private Integer step;
    private Integer heartRate;
    private ConditionStatus conditionStatus;

    public static ResponseLeaveWorkDto from(Health health) {
        return ResponseLeaveWorkDto.builder()
                .workTime(health.getWorkTime())
                .leaveWorkTime(health.getLeaveWorkTime())
                .finish1(health.getFinish1())
                .finish2(health.getFinish2())
                .finish3(health.getFinish3())
                .step(health.getStep())
                .heartRate(health.getHeartRate())
                .conditionStatus(health.getConditionStatus())
                .build();
    }
}
