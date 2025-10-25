package sansam.shimbox.driver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import sansam.shimbox.product.enums.ShippingStatus;

@Getter
@AllArgsConstructor
public class RequestUpdateShippingStatusDto {

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "변경할 배송 상태", example = "배송시작")
    private ShippingStatus status;

    @Schema(description = "배송 상태 변경 할 때의 위치 (선택사항 - WebSocket에서 자동으로 가져옴)", example = "동양미래대학교 ~")
    private String location;

    @Schema(description = "위도 (선택사항 - WebSocket에서 자동으로 가져옴)", example = "37.501")
    private Double latitude;

    @Schema(description = "경도 (선택사항 - WebSocket에서 자동으로 가져옴)", example = "127.037")
    private Double longitude;

    @Schema(description = "주소 (선택사항 - WebSocket에서 자동으로 가져옴)", example = "강남구 역삼동")
    private String addressShort;

    @Schema(description = "지역 (선택사항 - WebSocket에서 자동으로 가져옴)", example = "신도림동")
    private String region;
}
