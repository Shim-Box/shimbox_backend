package sansam.shimbox.driver.dto.response.record;

import java.util.List;

public record DeliveryGroupDto(
        String shippingLocation,
        int totalCount,
        int completedCount,
        int inProgressCount,
        List<DeliverySubGroupDto> groups
) {}