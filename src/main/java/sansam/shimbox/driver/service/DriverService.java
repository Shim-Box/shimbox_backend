package sansam.shimbox.driver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.domain.DriverRegion;
import sansam.shimbox.driver.domain.Health;
import sansam.shimbox.driver.dto.request.RequestLeaveWorkDto;
import sansam.shimbox.driver.dto.request.RequestSaveImageUrlDto;
import sansam.shimbox.driver.dto.request.RequestUpdateShippingStatusDto;
import sansam.shimbox.driver.dto.response.*;
import sansam.shimbox.driver.dto.response.record.DeliveryGroupDto;
import sansam.shimbox.driver.dto.response.record.DeliveryLocationSummaryDto;
import sansam.shimbox.driver.dto.response.record.DeliverySubGroupDto;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.repository.DriverRegionRepository;
import sansam.shimbox.driver.repository.DriverRepository;
import sansam.shimbox.driver.repository.HealthRepository;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.product.domain.Product;
import sansam.shimbox.product.dto.ProductDto;
import sansam.shimbox.product.enums.ShippingStatus;
import sansam.shimbox.product.repository.ProductRepository;
import sansam.shimbox.product.service.ProductService;
import sansam.shimbox.location.service.LocationRoomService;
import sansam.shimbox.location.domain.LocationData;
import sansam.shimbox.location.dto.response.MessageDriverHealth;

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
    private final DriverRegionRepository driverRegionRepository;
    private final HealthRepository healthRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final LocationRoomService locationRoomService;

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

        // LocationRoomService에서 실시간 건강 데이터 조회
        MessageDriverHealth.Payload realtimeHealth = locationRoomService.getDriverHealth(userId);
        
        todayHealth.markOffWork(
                LocalDateTime.now(),
                dto.getFinish1(),
                dto.getFinish2(),
                dto.getFinish3()
        );

        // 실시간 건강 데이터가 있으면 업데이트
        if (realtimeHealth != null) {
            todayHealth.updateRealtimeMetrics(
                    realtimeHealth.step(),
                    realtimeHealth.heartRate(),
                    null // conditionStatus는 제거되었으므로 null
            );
        }

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

        return ResponseLeaveWorkDto.from(todayHealth);
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

        // WebSocket에서 현재 위치 가져와서 DB에 저장
        LocationData currentLocation = locationRoomService.getDriverLocation(userId);
        if (currentLocation != null) {
            productService.saveProductStatusChangeLocation(
                dto.getProductId(), 
                requested, 
                currentLocation.getLat(), 
                currentLocation.getLng(), 
                currentLocation.getAddressShort()
            );
        } else {
            // WebSocket에서 위치를 가져올 수 없는 경우, DTO에서 받은 위치 사용
            if (dto.getLatitude() != null && dto.getLongitude() != null) {
                productService.saveProductStatusChangeLocation(
                    dto.getProductId(), 
                    requested, 
                    dto.getLatitude(), 
                    dto.getLongitude(), 
                    dto.getAddressShort()
                );
            }
        }

        // 배달 완료 시 배달 건수 업데이트
        if (requested == ShippingStatus.COMPLETED) {
            updateDeliveryCount(userId);
        }

        return new ResponseShippingStatusDto(product.getProductId(), product.getShippingStatus());
    }

    /**
     * 기사 배정 지역 조회
     * 
     * @param userId 기사 사용자 ID
     * @return 기사가 배정받은 지역 목록
     * @throws CustomException 기사가 존재하지 않거나 배정된 지역이 없는 경우
     */
    public List<ResponseDriverRegionInfoDto> getDriverAssignedRegion(Long userId) {
        
        // 기사 조회
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));
        
        // 기사 배정 지역 조회
        List<DriverRegion> assignments = driverRegionRepository
                .findByDriverOrderByCreatedDateAsc(driver);
        
        if (assignments.isEmpty()) {
            throw new CustomException(ErrorCode.DRIVER_REGION_NOT_FOUND);
        }
        
        // 지역 정보를 DTO로 변환
        List<ResponseDriverRegionInfoDto> regions = assignments.stream()
                .map(assignment -> ResponseDriverRegionInfoDto.from(assignment.getRegion()))
                .toList();
        
        return regions;
    }

    /**
     * 배달 완료 시 배달 건수 업데이트
     * 
     * @param userId 기사 사용자 ID
     */
    @Transactional
    public void updateDeliveryCount(Long userId) {
        // 기사 조회
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        // 기사의 최신 건강 데이터 조회
        Health latestHealth = healthRepository.findTopByDriverOrderByCreatedDateDesc(driver)
                .orElse(null);

        if (latestHealth != null) {
            // 기존 배달 건수에 1 추가
            int currentCount = latestHealth.getDeliveryCount() != null ? latestHealth.getDeliveryCount() : 0;
            latestHealth.updateDeliveryCount(currentCount + 1);
            healthRepository.save(latestHealth);
        }
    }

    /**
     * 기사의 최근 5일 근무 통계 조회
     * 
     * @param userId 기사 사용자 ID
     * @return 5일 근무 통계 정보
     */
    @Transactional(readOnly = true)
    public ResponseWeeklyWorkStatsDto getWeeklyWorkStats(Long userId) {
        // 기사 조회
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        // 5일 전 날짜 계산
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(5);

        // 5일 동안의 건강 데이터 조회 (근무 시간 포함)
        List<Health> weeklyHealthData = healthRepository.findByDriverAndCreatedDateBetweenAndIsDeletedFalse(
                driver, startDate, endDate);

        // 근무한 날짜들만 필터링 (출근시간과 퇴근시간이 모두 있는 데이터)
        List<Health> workedDays = weeklyHealthData.stream()
                .filter(health -> health.getWorkTime() != null && health.getLeaveWorkTime() != null)
                .sorted(Comparator.comparing(Health::getWorkTime).reversed()) // 최신 날짜부터
                .limit(5) // 최근 5일만
                .toList();

        // 일별 통계 계산
        List<ResponseWeeklyWorkStatsDto.DailyWorkStats> dailyStats = workedDays.stream()
                .map(health -> {
                    // 근무 시간 계산 (분 단위)
                    Duration workDuration = Duration.between(health.getWorkTime(), health.getLeaveWorkTime());
                    long workMinutes = workDuration.toMinutes();

                    return ResponseWeeklyWorkStatsDto.DailyWorkStats.builder()
                            .date(health.getWorkTime().toLocalDate().toString())
                            .workMinutes(workMinutes)
                            .deliveryCount(health.getDeliveryCount() != null ? health.getDeliveryCount() : 0)
                            .workStartTime(health.getWorkTime())
                            .workEndTime(health.getLeaveWorkTime())
                            .build();
                })
                .sorted(Comparator.comparing(ResponseWeeklyWorkStatsDto.DailyWorkStats::getDate)) // 날짜 순으로 정렬
                .toList();

        // 총계 계산
        long totalWorkMinutes = dailyStats.stream()
                .mapToLong(ResponseWeeklyWorkStatsDto.DailyWorkStats::getWorkMinutes)
                .sum();

        int totalDeliveryCount = dailyStats.stream()
                .mapToInt(ResponseWeeklyWorkStatsDto.DailyWorkStats::getDeliveryCount)
                .sum();

        // 평균 계산 (실제 근무한 날짜 수로 계산)
        int actualWorkingDays = dailyStats.size();
        double averageDailyWorkMinutes = actualWorkingDays > 0 ? (double) totalWorkMinutes / actualWorkingDays : 0.0;
        double averageDailyDeliveryCount = actualWorkingDays > 0 ? (double) totalDeliveryCount / actualWorkingDays : 0.0;

        return ResponseWeeklyWorkStatsDto.from(driver, startDate, endDate, dailyStats,
                totalWorkMinutes, totalDeliveryCount, averageDailyWorkMinutes, averageDailyDeliveryCount);
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
