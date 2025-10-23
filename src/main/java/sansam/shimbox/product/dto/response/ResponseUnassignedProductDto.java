package sansam.shimbox.product.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import sansam.shimbox.product.domain.Product;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUnassignedProductDto {
    private Long productId;
    private String productName;
    private String recipientName;
    private String recipientPhoneNumber;
    private String address;
    private String detailAddress;
    private String postalCode;
    private String deliveryImageUrl;

    public static ResponseUnassignedProductDto from(Product product) {
        return ResponseUnassignedProductDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .recipientName(product.getRecipientName())
                .recipientPhoneNumber(product.getRecipientPhoneNumber())
                .address(product.getAddress())
                .detailAddress(product.getDetailAddress())
                .postalCode(product.getPostalCode())
                .deliveryImageUrl(product.getDeliveryImageUrl())
                .build();
    }
}
