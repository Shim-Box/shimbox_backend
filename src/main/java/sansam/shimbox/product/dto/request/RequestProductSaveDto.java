package sansam.shimbox.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import sansam.shimbox.product.enums.ShippingStatus;

@Getter
@Setter
public class RequestProductSaveDto {

    @Schema(description = "기사 ID", example = "1")
    @NotNull
    private Long driverId;

    @Schema(description = "배송상태", example = "배송대기")
    private ShippingStatus shippingStatus;

    @Schema(description = "상품명", example = "세탁세제")
    @NotBlank
    private String productName;

    @Schema(description = "수령인 이름", example = "홍길동")
    @NotBlank
    private String recipientName;

    @Schema(description = "수령인 전화번호", example = "010-1234-5678")
    @NotBlank
    private String recipientPhoneNumber;

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로")
    @NotBlank
    private String address;

    @Schema(description = "상세 주소", example = "101동 202호")
    @NotBlank
    private String detailAddress;

    @Schema(description = "우편번호", example = "06236")
    @NotBlank
    private String postalCode;
}