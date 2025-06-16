package sansam.shimbox.driver.dto.response.record;

public record DeliveryLocationSummaryDto(
        String shippingLocation,
        int totalCount,
        int completedCount,
        int pendingCount
) {}