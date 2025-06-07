package sansam.shimbox.driver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sansam.shimbox.product.enums.ShippingStatus;

@Getter
@AllArgsConstructor
public class ResponseShippingStatusDto {
    Long productId;
    ShippingStatus newStatus;
}
