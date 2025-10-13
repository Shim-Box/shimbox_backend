package sansam.shimbox.product.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import sansam.shimbox.product.domain.Product;
import sansam.shimbox.product.enums.ShippingStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseProductDto {
    private Long productId;
    private String productName;
    private String recipientName;
    private String recipientPhoneNumber;
    private String address;
    private String detailAddress;
    private String postalCode;
    private ShippingStatus shippingStatus;
    private Long driverId;

    public static ResponseProductDto from(Product product) {
        return ResponseProductDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .recipientName(product.getRecipientName())
                .recipientPhoneNumber(product.getRecipientPhoneNumber())
                .address(product.getAddress())
                .detailAddress(product.getDetailAddress())
                .postalCode(product.getPostalCode())
                .shippingStatus(product.getShippingStatus())
                .driverId(product.getDriver().getDriverId())
                .build();
    }
}
