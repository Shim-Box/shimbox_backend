package sansam.shimbox.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import sansam.shimbox.product.enums.ShippingStatus;

@Builder
@Getter
public class ResponseDriverProductDto {
    private Long productId;
    private String productName;
    private String recipientName;
    private String recipientPhoneNumber;
    private String address;
    private String detailAddress;
    private String postalCode;
    private String deliveryImageUrl;
    private ShippingStatus shippingStatus;
}
