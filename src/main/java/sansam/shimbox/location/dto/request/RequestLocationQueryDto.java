package sansam.shimbox.location.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import sansam.shimbox.driver.enums.Region;
import lombok.Data;

@Data
public class RequestLocationQueryDto {
    
    @Schema(description = "지역 필터링", example = "구로구")
    private Region region;
}
