package sansam.shimbox.driver.dto.response;

public record DeliveryLocationSummaryDto(
        String shippingLocation,
        int totalCount,
        int completedCount,
        int pendingCount
) {}