package sansam.shimbox.driver.dto.response;

import sansam.shimbox.product.dto.ProductDto;

import java.util.List;

public record DeliveryGroupDto(
        String shippingLocation,
        int totalCount,
        int completedCount,
        int inProgressCount,
        List<DeliverySubGroupDto> groups
) {}