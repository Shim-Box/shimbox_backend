package sansam.shimbox.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestProductCreateDto {

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
