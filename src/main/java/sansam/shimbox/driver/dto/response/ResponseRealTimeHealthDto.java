package sansam.shimbox.driver.dto.response;

import lombok.Builder;
import lombok.Getter;
import sansam.shimbox.driver.enums.ConditionStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResponseRealTimeHealthDto {
    private Long driverId;
    private Integer step;
    private Integer heartRate;
    private ConditionStatus conditionStatus;
    private LocalDateTime modifiedDate;
}
