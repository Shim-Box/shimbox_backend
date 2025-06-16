package sansam.shimbox.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import sansam.shimbox.product.enums.ShippingStatus;

@Getter
@Builder
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
}
