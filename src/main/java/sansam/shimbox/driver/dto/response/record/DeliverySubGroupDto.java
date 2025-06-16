package sansam.shimbox.driver.dto.response.record;

import sansam.shimbox.product.dto.ProductDto;

import java.util.List;

public record DeliverySubGroupDto(
        String detailAddress,
        int count,
        int completedCount,
        int inProgressCount,
        List<ProductDto> products
) {}