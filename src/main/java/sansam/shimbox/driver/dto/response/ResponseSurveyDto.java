package sansam.shimbox.driver.dto.response;

import lombok.Builder;
import lombok.Getter;
import sansam.shimbox.driver.dto.request.RequestSurveyDto;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.driver.enums.Finish1;
import sansam.shimbox.driver.enums.Finish2;
import sansam.shimbox.driver.enums.Finish3;

@Getter
@Builder
public class ResponseSurveyDto {
    private Finish1 finish1;
    private Finish2 finish2;
    private Finish3 finish3;
    private Integer step;
    private Integer heartRate;
    private ConditionStatus conditionStatus;

    public static ResponseSurveyDto from(RequestSurveyDto dto) {
        return ResponseSurveyDto.builder()
                .finish1(dto.getFinish1())
                .finish2(dto.getFinish2())
                .finish3(dto.getFinish3())
                .step(dto.getStep())
                .heartRate(dto.getHeartRate())
                .conditionStatus(dto.getConditionStatus())
                .build();
    }
}

