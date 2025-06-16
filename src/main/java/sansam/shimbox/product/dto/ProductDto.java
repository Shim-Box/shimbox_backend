package sansam.shimbox.product.dto;

import sansam.shimbox.product.enums.ShippingStatus;

import java.time.LocalDateTime;

public record ProductDto(
        Long productId,
        String productName,
        String recipientName,
        String address,
        String detailAddress,
        ShippingStatus shippingStatus
) {}