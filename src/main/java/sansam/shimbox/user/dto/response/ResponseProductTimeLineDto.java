package sansam.shimbox.user.dto.response;

import sansam.shimbox.product.enums.ShippingStatus;

import java.time.LocalDateTime;

public record ResponseProductTimeLineDto(
        Long productTimeLineId,
        ShippingStatus prevStatus,
        String location,
        LocalDateTime createDate
) {}