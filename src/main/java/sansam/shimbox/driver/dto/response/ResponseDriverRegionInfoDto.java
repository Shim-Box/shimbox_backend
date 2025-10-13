package sansam.shimbox.driver.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import sansam.shimbox.driver.enums.Region;

@Getter
@Builder
public class ResponseDriverRegionInfoDto {

    @Schema(description = "지역명", example = "구로구")
    private String regionName;

    @Schema(description = "지역 코드", example = "GURO")
    private String regionCode;

    public static ResponseDriverRegionInfoDto from(Region region) {
        return ResponseDriverRegionInfoDto.builder()
                .regionName(region.getLabel())
                .regionCode(region.name())
                .build();
    }
}
