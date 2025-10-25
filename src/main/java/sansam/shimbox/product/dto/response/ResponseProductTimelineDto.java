package sansam.shimbox.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import sansam.shimbox.product.domain.ProductTimeLine;
import sansam.shimbox.product.enums.ShippingStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseProductTimelineDto {

    @Schema(description = "타임라인 ID", example = "1")
    private Long timelineId;

    @Schema(description = "상품 ID", example = "1")
    private Long productId;

    @Schema(description = "상품명", example = "아이폰 15")
    private String productName;

    @Schema(description = "배송 상태", example = "STARTED")
    private ShippingStatus status;

    @Schema(description = "상태 변경 시간 (한국 시간)", example = "2025-01-20T19:30:00+09:00")
    private String statusChangedAt;

    @Schema(description = "위도", example = "37.501")
    private Double latitude;

    @Schema(description = "경도", example = "127.037")
    private Double longitude;

    @Schema(description = "주소 (간단)", example = "강남구 역삼동")
    private String addressShort;

    @Schema(description = "기사명", example = "김기사")
    private String driverName;

    @Schema(description = "기사 전화번호", example = "010-1234-5678")
    private String driverPhoneNumber;

    public static ResponseProductTimelineDto from(ProductTimeLine timeline) {
        return ResponseProductTimelineDto.builder()
                .timelineId(timeline.getTimeLineId())
                .productId(timeline.getProduct().getProductId())
                .productName(timeline.getProduct().getProductName())
                .status(timeline.getStatus())
                .statusChangedAt(timeline.getStatusChangedAt())
                .latitude(timeline.getLatitude())
                .longitude(timeline.getLongitude())
                .addressShort(timeline.getAddressShort())
                .driverName(timeline.getProduct().getDriver().getUser().getName())
                .driverPhoneNumber(timeline.getProduct().getDriver().getUser().getPhoneNumber())
                .build();
    }
}
