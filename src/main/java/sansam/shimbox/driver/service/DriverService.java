package sansam.shimbox.driver.service;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Request;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.domain.DriverRealtime;
import sansam.shimbox.driver.domain.Health;
import sansam.shimbox.driver.dto.request.RequestSaveImageUrlDto;
import sansam.shimbox.driver.dto.request.RequestUpdateShippingStatusDto;
import sansam.shimbox.driver.dto.response.*;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.enums.Region;
import sansam.shimbox.driver.repository.DriverRepository;
import sansam.shimbox.driver.repository.HealthRepository;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.product.domain.Product;
import sansam.shimbox.product.domain.Shipp;
import sansam.shimbox.product.dto.ProductDto;
import sansam.shimbox.product.enums.ShippingStatus;
import sansam.shimbox.product.repository.ShippRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final HealthRepository healthRepository;
    private final ShippRepository shippRepository;

    @Transactional
    public ResponseAttendanceDto updateAttendanceStatus(Long userId, Attendance requestedStatus) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        // 자동 상태 초기화 로직
        if (driver.getAttendance() == Attendance.OFF_WORK) {
            Health recentHealth = healthRepository.findTopByDriverOrderByCreatedDateDesc(driver)
                    .orElse(null);

            if (recentHealth != null && recentHealth.getLeaveWorkTime() != null) {
                LocalDateTime leaveTime = recentHealth.getLeaveWorkTime();

                if (leaveTime.toLocalDate().isBefore(now.toLocalDate()) ||
                        Duration.between(leaveTime, now).toMinutes() >= 1) {
                    driver.changeAttendanceOnly(Attendance.BEFORE_WORK);
                    driver.changeWorkTime(null);
                }
            }
        }

        Attendance currentStatus = getAttendance(requestedStatus, driver);
        if (currentStatus == Attendance.OFF_WORK) {
            throw new CustomException(ErrorCode.INVALID_ATTENDANCE_TRANSITION);
        }

        // 근무상태 전이 처리
        driver.changeAttendanceOnly(requestedStatus);
        driver.changeWorkTime(now);

        if (requestedStatus == Attendance.WORKING) {
            if (driver.getDriverRealtime() == null) {
                driver.assignDriverRealtime(DriverRealtime.createNew(driver));
            }

            Health health = Health.builder()
                    .driver(driver)
                    .workTime(now)
                    .isDeleted(false)
                    .build();
            healthRepository.save(health);

        } else if (requestedStatus == Attendance.OFF_WORK) {
            Health todayHealth = healthRepository.findTopByDriverOrderByCreatedDateDesc(driver)
                    .orElseThrow(() -> new CustomException(ErrorCode.HEALTH_RECORD_NOT_FOUND));

            todayHealth.markLeaveTime(now);
        }

        driverRepository.save(driver);

        return new ResponseAttendanceDto(
                driver.getAttendance(),
                now
        );
    }

    //배정 받은 지역 조회
    public List<DeliveryLocationSummaryDto> getAssignedLocationSummary(Long userId) {
        Map<String, List<Product>> groupedByLocation = getGroupedProductsByLocation(userId);

        return groupedByLocation.entrySet().stream()
                .map(entry -> {
                    String location = entry.getKey();
                    List<Product> products = entry.getValue();
                    int total = products.size();
                    int completed = (int) products.stream()
                            .filter(p -> p.getShipp().getShippingStatus() == ShippingStatus.COMPLETED)
                            .count();
                    int pending = total - completed;
                    return new DeliveryLocationSummaryDto(location, total, completed, pending);
                })
                .sorted(Comparator.comparing(DeliveryLocationSummaryDto::shippingLocation))
                .toList();
    }

    //지역별 배송 상품 조회
    public List<DeliveryGroupDto> getGroupedDeliverySummaryByDriver(Long userId) {
        List<Shipp> shipps = shippRepository.findAllByIsDeletedFalseAndDriver_DriverId(userId);
        List<Product> products = shipps.stream().map(Shipp::getProduct).toList();

        if (products.isEmpty()) {
            throw new CustomException(ErrorCode.DELIVERY_NOT_FOUND);
        }

        Map<String, List<Product>> groupedByLocation = products.stream()
                .collect(Collectors.groupingBy(Product::getShippingLocation));

        return groupedByLocation.entrySet().stream()
                .map(entry -> {
                    String location = entry.getKey();
                    List<Product> locationProducts = entry.getValue();

                    Map<String, List<Product>> subGroup = locationProducts.stream()
                            .collect(Collectors.groupingBy(Product::getDetailAddress));

                    List<DeliverySubGroupDto> detailGroups = subGroup.entrySet().stream()
                            .map(subEntry -> {
                                List<Product> grouped = subEntry.getValue();
                                int total = grouped.size();
                                int completed = (int) grouped.stream().filter(p -> p.getShipp().getShippingStatus() == ShippingStatus.COMPLETED).count();
                                int inProgress = (int) grouped.stream().filter(p -> p.getShipp().getShippingStatus() == ShippingStatus.STARTED).count();
                                List<ProductDto> dtos = grouped.stream().map(this::toDto).toList();

                                return new DeliverySubGroupDto(subEntry.getKey(), total, completed, inProgress, dtos);
                            })
                            .toList();

                    int totalCount = detailGroups.stream().mapToInt(DeliverySubGroupDto::count).sum();
                    int completedCount = detailGroups.stream().mapToInt(DeliverySubGroupDto::completedCount).sum();
                    int inProgressCount = detailGroups.stream().mapToInt(DeliverySubGroupDto::inProgressCount).sum();

                    return new DeliveryGroupDto(location, totalCount, completedCount, inProgressCount, detailGroups);
                })
                .toList();
    }


    //배송 상태 변경
    @Transactional
    public ResponseShippingStatusDto updateShippingStatus(Long userId, RequestUpdateShippingStatusDto dto) {
        Shipp shipp = shippRepository.findByProduct_ProductId(dto.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.SHIPP_NOT_FOUND));

        if (!shipp.getDriver().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        ShippingStatus current = shipp.getShippingStatus();
        ShippingStatus requested = dto.getStatus();

        if (current == ShippingStatus.WAITING && requested == ShippingStatus.STARTED ||
                current == ShippingStatus.STARTED && requested == ShippingStatus.COMPLETED) {
            shipp.setShippingStatus(requested);
        } else {
            throw new CustomException(ErrorCode.INVALID_SHIPPING_TRANSITION);
        }

        shippRepository.save(shipp);

        return new ResponseShippingStatusDto(shipp.getProduct().getProductId(), shipp.getShippingStatus());
    }

    //배송 도착 이미지 저장
    @Transactional
    public String saveDeliveryImageUrl(Long userId, RequestSaveImageUrlDto dto) {
        Shipp shipp = shippRepository.findByProduct_ProductId(dto.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.SHIPP_NOT_FOUND));

        if (!shipp.getDriver().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (shipp.getShippingStatus() != ShippingStatus.COMPLETED) {
            throw new CustomException(ErrorCode.INVALID_SHIPPING_TRANSITION);
        }

        Shipp updated = shipp.toBuilder()
                .deliveryImageUrl(dto.getImageUrl())
                .build();

        shippRepository.save(updated);
        return dto.getImageUrl();
    }

    private Map<String, List<Product>> getGroupedProductsByLocation(Long userId) {
        List<Shipp> shipps = shippRepository.findAllByIsDeletedFalseAndDriver_DriverId(userId);

        if (shipps.isEmpty()) {
            throw new CustomException(ErrorCode.DELIVERY_NOT_FOUND);
        }

        return shipps.stream()
                .map(Shipp::getProduct)
                .collect(Collectors.groupingBy(Product::getShippingLocation));
    }

    private ProductDto toDto(Product product) {
        return new ProductDto(
                product.getProductId(),
                product.getProductName(),
                product.getRecipientName(),
                product.getAddress(),
                product.getDetailAddress(),
                product.getEstimatedArrivalTime(),
                product.getShipp().getShippingStatus()
        );
    }

    // 근무상태 유효성 검사
    private static Attendance getAttendance(Attendance requestedStatus, Driver driver) {
        Attendance currentStatus = driver.getAttendance();

        if (currentStatus == requestedStatus) {
            throw new CustomException(ErrorCode.ALREADY_IN_SAME_STATUS);
        }
        if (currentStatus == Attendance.BEFORE_WORK && requestedStatus == Attendance.OFF_WORK) {
            throw new CustomException(ErrorCode.INVALID_ATTENDANCE_TRANSITION);
        }
        if (currentStatus == Attendance.WORKING && requestedStatus != Attendance.OFF_WORK) {
            throw new CustomException(ErrorCode.INVALID_ATTENDANCE_TRANSITION);
        }
        return currentStatus;
    }

}
