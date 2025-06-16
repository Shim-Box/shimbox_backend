package sansam.shimbox.driver.dto.response;

import lombok.Builder;
import lombok.Getter;
import sansam.shimbox.driver.enums.Finish1;
import sansam.shimbox.driver.enums.Finish2;
import sansam.shimbox.driver.enums.Finish3;

@Getter
@Builder
public class ResponseSurveySaveDto {
    private Finish1 finish1;
    private Finish2 finish2;
    private Finish3 finish3;
}
