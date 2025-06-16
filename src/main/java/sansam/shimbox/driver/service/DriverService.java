package sansam.shimbox.driver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.domain.Health;
import sansam.shimbox.driver.dto.request.RequestLeaveWorkDto;
import sansam.shimbox.driver.dto.request.RequestSaveImageUrlDto;
import sansam.shimbox.driver.dto.request.RequestUpdateShippingStatusDto;
import sansam.shimbox.driver.dto.response.*;
import sansam.shimbox.driver.dto.response.record.DeliveryGroupDto;
import sansam.shimbox.driver.dto.response.record.DeliveryLocationSummaryDto;
import sansam.shimbox.driver.dto.response.record.DeliverySubGroupDto;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.repository.DriverRepository;
import sansam.shimbox.driver.repository.HealthRepository;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.product.domain.Product;
import sansam.shimbox.product.dto.ProductDto;
import sansam.shimbox.product.enums.ShippingStatus;
import sansam.shimbox.product.repository.ProductRepository;

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
    private final ProductRepository productRepository;
    private final RealtimeHealthService realtimeHealthService;

    //기사 근태 변경
    @Transactional
    public ResponseAttendanceDto updateAttendanceStatus(Long userId, Attendance requestedStatus) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        resetAttendanceIfExpired(driver, now); // 자동 초기화 처리

        Attendance currentStatus = getAttendance(requestedStatus, driver);
        if (currentStatus == Attendance.OFF_WORK) {
            throw new CustomException(ErrorCode.INVALID_ATTENDANCE_TRANSITION);
        }

        // 상태 변경 및 기록
        driver.changeAttendanceOnly(requestedStatus);

        if (requestedStatus == Attendance.WORKING) {
            driver.changeWorkTime(now);

            healthRepository.save(Health.builder()
                    .driver(driver)
                    .workTime(now)
                    .isDeleted(false)
                    .build());
        }
        driverRepository.save(driver);

        return new ResponseAttendanceDto(driver.getAttendance(), now);
    }

    //건강 설문 저장
    @Transactional
    public ResponseSurveySaveDto leaveWorkHealthSave(Long userId, RequestLeaveWorkDto dto) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        Health todayHealth = healthRepository.findTopByDriverOrderByCreatedDateDesc(driver)
                .orElseThrow(() -> new CustomException(ErrorCode.HEALTH_RECORD_NOT_FOUND));

        ResponseRealTimeHealthDto realtime = realtimeHealthService.getRealtimeHealth(userId);

        todayHealth.markOffWork(
                LocalDateTime.now(),
                dto.getFinish1(),
                dto.getFinish2(),
                dto.getFinish3()
        );

        todayHealth.updateRealtimeMetrics(
                realtime.getStep(),
                realtime.getHeartRate(),
                realtime.getConditionStatus()
        );

        // 저장 후 실시간 데이터 삭제
        realtimeHealthService.deleteRealtimeHealth(userId);

        return ResponseSurveySaveDto.builder()
                .finish1(dto.getFinish1())
                .finish2(dto.getFinish2())
                .finish3(dto.getFinish3())
                .build();
    }

    //톼근 후 기사 건강 데이터 조회
    @Transactional(readOnly = true)
    public ResponseLeaveWorkDto getTodayHealthSummary(Long userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        Health todayHealth = healthRepository
                .findTopByDriverAndLeaveWorkTimeIsNotNullOrderByLeaveWorkTimeDesc(driver)
                .orElseThrow(() -> new CustomException(ErrorCode.HEALTH_RECORD_NOT_FOUND));

        return ResponseLeaveWorkDto.builder()
                .workTime(todayHealth.getWorkTime())
                .leaveWorkTime(todayHealth.getLeaveWorkTime())
                .finish1(todayHealth.getFinish1())
                .finish2(todayHealth.getFinish2())
                .finish3(todayHealth.getFinish3())
                .step(todayHealth.getStep())
                .heartRate(todayHealth.getHeartRate())
                .conditionStatus(todayHealth.getConditionStatus())
                .build();
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
                            .filter(p -> p.getShippingStatus() == ShippingStatus.COMPLETED)
                            .count();
                    int pending = total - completed;
                    return new DeliveryLocationSummaryDto(location, total, completed, pending);
                })
                .sorted(Comparator.comparing(DeliveryLocationSummaryDto::shippingLocation))
                .toList();
    }

    //지역별 배송 상품 조회
    public List<DeliveryGroupDto> getGroupedDeliverySummaryByDriver(Long userId) {
        List<Product> products = productRepository.findActiveProductsByDriverId(userId);

        if (products.isEmpty()) {
            throw new CustomException(ErrorCode.DELIVERY_NOT_FOUND);
        }

        Map<String, List<Product>> groupedByLocation = products.stream()
                .collect(Collectors.groupingBy(Product::getAddress));

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
                                int completed = (int) grouped.stream().filter(p -> p.getShippingStatus() == ShippingStatus.COMPLETED).count();
                                int inProgress = (int) grouped.stream().filter(p -> p.getShippingStatus() == ShippingStatus.STARTED).count();
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
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.SHIPP_NOT_FOUND));

        if (!product.getDriver().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        ShippingStatus current = product.getShippingStatus();
        ShippingStatus requested = dto.getStatus();

        if (current == ShippingStatus.WAITING && requested == ShippingStatus.STARTED ||
                current == ShippingStatus.STARTED && requested == ShippingStatus.COMPLETED) {
            product.setShippingStatus(requested);
        } else {
            throw new CustomException(ErrorCode.INVALID_SHIPPING_TRANSITION);
        }

        productRepository.save(product);

        return new ResponseShippingStatusDto(product.getProductId(), product.getShippingStatus());
    }

    //배송 도착 이미지 저장
    @Transactional
    public String saveDeliveryImageUrl(Long userId, RequestSaveImageUrlDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.SHIPP_NOT_FOUND));

        if (!product.getDriver().getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        if (product.getShippingStatus() != ShippingStatus.COMPLETED) {
            throw new CustomException(ErrorCode.INVALID_SHIPPING_TRANSITION);
        }

        Product updated = Product.builder()
                .deliveryImageUrl(dto.getImageUrl())
                .build();

        productRepository.save(updated);
        return dto.getImageUrl();
    }

    private Map<String, List<Product>> getGroupedProductsByLocation(Long userId) {
        List<Product> shipps = productRepository.findActiveProductsByDriverUserId(userId);

        if (shipps.isEmpty()) {
            throw new CustomException(ErrorCode.DELIVERY_NOT_FOUND);
        }

        return shipps.stream()
                .collect(Collectors.groupingBy(Product::getAddress));
    }

    private ProductDto toDto(Product product) {
        return new ProductDto(
                product.getProductId(),
                product.getProductName(),
                product.getRecipientName(),
                product.getAddress(),
                product.getDetailAddress(),
                product.getShippingStatus()
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

    //근무 상태 초기화
    private void resetAttendanceIfExpired(Driver driver, LocalDateTime now) {
        if (driver.getAttendance() != Attendance.OFF_WORK) return;

        healthRepository.findTopByDriverOrderByCreatedDateDesc(driver)
                .filter(h -> h.getLeaveWorkTime() != null)
                .map(Health::getLeaveWorkTime)
                .filter(leaveTime -> Duration.between(leaveTime, now).toMinutes() >= 1)
                .ifPresent(leaveTime -> {
                    driver.changeAttendanceOnly(Attendance.BEFORE_WORK);
                    driver.changeWorkTime(null);
                });
    }

}
