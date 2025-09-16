package sansam.shimbox.driver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import sansam.shimbox.product.enums.ShippingStatus;

@Getter
public class RequestUpdateShippingStatusDto {

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "변경할 배송 상태", example = "배송시작")
    private ShippingStatus status;

    @Schema(description = "배송 상태 변경 할 때의 위치", example = "동양미래대학교 ~")
    private String location;
}
