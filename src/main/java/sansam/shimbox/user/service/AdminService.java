package sansam.shimbox.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.auth.repository.UserRepository;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.domain.DriverRegion;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.driver.enums.Region;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.repository.DriverRegionRepository;
import sansam.shimbox.driver.repository.DriverRepository;
import sansam.shimbox.global.redis.RedisService;
import sansam.shimbox.product.enums.ShippingStatus;
import sansam.shimbox.user.dto.request.RequestDriverRegionDto;
import sansam.shimbox.user.dto.request.RequestUserStatusDto;
import sansam.shimbox.user.dto.response.ResponseDriverRegionDto;
import sansam.shimbox.user.dto.response.ResponseUserApprovedDto;
import sansam.shimbox.user.dto.response.ResponseUserPendingDto;
import sansam.shimbox.user.dto.response.ResponseDriverProfileDto;
import sansam.shimbox.product.domain.Product;
import sansam.shimbox.product.dto.response.ResponseProductDto;
import sansam.shimbox.product.dto.response.ResponseUnassignedProductDto;
import sansam.shimbox.product.repository.ProductRepository;
import sansam.shimbox.global.common.PagedResponse;
import sansam.shimbox.global.common.RequestPagingDto;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final DriverRegionRepository driverRegionRepository;
    private final ProductRepository productRepository;
    private final RedisService redisService;

    //가입 대기자 조회
    public PagedResponse<ResponseUserPendingDto> userFindAll(RequestPagingDto pagingDto) {
        Page<User> usersPage = userRepository.findAllByApprovalStatusFalse(pagingDto.toPageable());

        Page<ResponseUserPendingDto> dtoPage = usersPage.map(ResponseUserPendingDto::from);

        return new PagedResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber() + 1,
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        );
    }

    //회원 승인
    @Transactional
    public List<Long> approveUser(RequestUserStatusDto dto) {
        List<Long> userIds = dto.getUserIds();
        List<User> users = userRepository.findAllById(userIds);

        if (users.size() != userIds.size()) {
            throw new CustomException(ErrorCode.USERS_NOT_FOUND);
        }

        List<Long> approvedIds = new ArrayList<>();

        for (User user : users) {
            if (!user.getApprovalStatus()) {
                user.approve();
                approvedIds.add(user.getId());

                if (user.getDriver() == null) {
                    Driver driver = Driver.builder()
                            .user(user)
                            .attendance(Attendance.BEFORE_WORK)
                            .workTime(null)
                            .isDeleted(false)
                            .build();

                    user.setDriver(driver);

                    driverRepository.save(driver);
                }
            }
        }

        return approvedIds;
    }

    //승인된 유저 조회
    public PagedResponse<ResponseUserApprovedDto> approvedUserFindAll(
            String residence, Attendance attendance, ConditionStatus conditionStatus, Pageable pageable) {

        Page<User> usersPage = userRepository.findApprovedUsersWithFilterWithoutConditionStatus(residence, attendance, pageable);

        List<ResponseUserApprovedDto> filteredList = usersPage.getContent().stream()
                .map(user -> {
                    Driver driver = user.getDriver();

                    String workTime = "-";
                    if (driver != null && driver.getWorkTime() != null) {
                        LocalDateTime startTime = driver.getWorkTime();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a hh:mm").withLocale(Locale.KOREA);
                        workTime = startTime.format(formatter);
                    }

                    int deliveries = driver != null && driver.getProducts() != null ?
                            (int) driver.getProducts().stream().filter(p -> p.getShippingStatus().isCompleted()).count() : 0;

                    int deliveryTarget = driver != null && driver.getProducts() != null ? driver.getProducts().size() : 0;

                    ConditionStatus realtimeStatus = driver != null ? redisService.getDriverConditionStatus(driver.getDriverId()) : null;

                    return ResponseUserApprovedDto.from(user, driver, workTime, deliveries, deliveryTarget, realtimeStatus);
                })
                .filter(dto -> conditionStatus == null || conditionStatus.equals(dto.getConditionStatus()))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                filteredList,
                usersPage.getNumber() + 1,
                usersPage.getSize(),
                filteredList.size(),
                (int) Math.ceil((double) filteredList.size() / usersPage.getSize())
        );
    }

    /**
     * 관리자가 특정 기사를 2개 지역에 배정
     * 
     * @param dto 기사 지역 배정 요청 정보
     * @return 배정 완료된 기사 지역 정보
     * @throws CustomException 기사가 존재하지 않는 경우
     * @throws CustomException 이미 지역에 배정된 기사인 경우
     * @throws CustomException 유효하지 않은 요청인 경우
     */
    @Transactional
    public ResponseDriverRegionDto assignDriverRegion(RequestDriverRegionDto dto) {
        // 기사 조회
        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        // 기존 지역 배정 확인
        List<DriverRegion> existingAssignments = driverRegionRepository
                .findByDriverOrderByCreatedDateAsc(driver);
        
        if (!existingAssignments.isEmpty()) {
            throw new CustomException(ErrorCode.DRIVER_ALREADY_ASSIGNED);
        }

        // 새로운 지역 배정 생성
        List<DriverRegion> newAssignments = new ArrayList<>();
        
        if (dto.getRegion1() != null) {
            DriverRegion assignment1 = DriverRegion.builder()
                    .driver(driver)
                    .region(dto.getRegion1())
                    .build();
            newAssignments.add(assignment1);
        }
        
        if (dto.getRegion2() != null) {
            DriverRegion assignment2 = DriverRegion.builder()
                    .driver(driver)
                    .region(dto.getRegion2())
                    .build();
            newAssignments.add(assignment2);
        }

        driverRegionRepository.saveAll(newAssignments);

        return ResponseDriverRegionDto.builder()
                .driverId(driver.getDriverId())
                .driverName(driver.getUser().getName())
                .region1(dto.getRegion1())
                .region2(dto.getRegion2())
                .build();
    }

    /**
     * 관리자가 특정 기사의 배정된 상품 목록 조회
     * 
     * @param driverId 기사 ID
     * @param shippingStatus 배송 상태 (null이면 모든 상태 조회)
     * @return 기사가 배정받은 상품 목록 (배송대기, 배송시작, 출근 후 완료된 상품들)
     */
    @Transactional(readOnly = true)
    public List<ResponseProductDto> getDriverProducts(Long driverId, ShippingStatus shippingStatus) {
        // 기사 존재 여부 확인
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        List<Product> products;
        
        if (shippingStatus == null) {
            // 모든 상태의 상품 조회 (배송대기, 배송시작, 배송완료)
            products = productRepository.findByDriverAndShippingStatusInAndIsDeletedFalse(
                    driver, 
                    List.of(ShippingStatus.WAITING, ShippingStatus.STARTED, ShippingStatus.COMPLETED)
            );
        } else {
            // 특정 상태의 상품만 조회
            products = productRepository.findByDriverAndShippingStatusAndIsDeletedFalse(driver, shippingStatus);
        }

        // 등록된 상품이 없는 경우
        if (products.isEmpty()) {
            throw new CustomException(ErrorCode.DELIVERY_NOT_FOUND);
        }

        // 현재 근무 중인 상품들만 필터링
        List<Product> currentWorkProducts = products.stream()
                .filter(product -> {
                    // 배송대기, 배송시작인 상품은 항상 포함 (현재 작업 중)
                    if (product.getShippingStatus() == ShippingStatus.WAITING ||
                        product.getShippingStatus() == ShippingStatus.STARTED) {
                        return true;
                    }
                    // 배송완료된 상품은 현재 근무 시간 내에 완료된 것만 포함
                    if (product.getShippingStatus() == ShippingStatus.COMPLETED) {
                        return isCompletedDuringCurrentWork(product, driver);
                    }
                    return false;
                })
                .toList();

        // 필터링 후에도 상품이 없는 경우
        if (currentWorkProducts.isEmpty()) {
            throw new CustomException(ErrorCode.DELIVERY_NOT_FOUND);
        }

        return currentWorkProducts.stream()
                .map(ResponseProductDto::from)
                .toList();
    }

    /**
     * 상품이 현재 근무 시간 내에 완료되었는지 확인
     * 
     * @param product 상품
     * @param driver 기사
     * @return 현재 근무 시간 내 완료 여부
     */
    private boolean isCompletedDuringCurrentWork(Product product, Driver driver) {
        // 기사가 현재 출근 상태인지 확인
        if (driver.getAttendance() != Attendance.WORKING) {
            // 출근 상태가 아니면 오늘 날짜 기준으로 확인
            return product.getCreatedDate().toLocalDate().equals(LocalDate.now());
        }
        
        // 기사의 출근 시간이 있는지 확인
        if (driver.getWorkTime() == null) {
            // 출근 시간이 없으면 오늘 날짜 기준으로 확인
            return product.getCreatedDate().toLocalDate().equals(LocalDate.now());
        }
        
        // 상품 완료 시간이 출근 시간 이후인지 확인
        return product.getModifiedDate().isAfter(driver.getWorkTime());
    }
    
    /**
     * 관리자가 특정 기사의 프로필 조회
     * 
     * @param driverId 기사 ID
     * @return 기사 프로필 정보
     */
    @Transactional(readOnly = true)
    public ResponseDriverProfileDto getDriverProfile(Long driverId) {
        // 기사 존재 여부 확인
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        // 기사의 배정된 지역 조회
        List<DriverRegion> regionAssignments = driverRegionRepository.findByDriverOrderByCreatedDateAsc(driver);
        List<Region> assignedRegions = regionAssignments.stream()
                .map(DriverRegion::getRegion)
                .toList();

        // 기사의 전체 배송 건수 조회 (오늘 할당받은 모든 상품)
        long totalDeliveryCount = productRepository.findByDriverAndShippingStatusInAndIsDeletedFalse(
                driver, 
                List.of(ShippingStatus.WAITING, ShippingStatus.STARTED, ShippingStatus.COMPLETED)
        ).stream()
        .filter(product -> product.getCreatedDate().toLocalDate().equals(LocalDate.now()))
        .count();

        // 기사의 완료된 배송 건수 조회 (오늘 완료된 상품 수)
        long completedDeliveryCount = productRepository.findByDriverAndShippingStatusAndIsDeletedFalse(driver, ShippingStatus.COMPLETED)
                .stream()
                .filter(product -> product.getModifiedDate().toLocalDate().equals(LocalDate.now()))
                .count();

        // 기사의 근무 시간 계산 (출근 시간부터 현재까지)
        Duration workDuration = calculateWorkDuration(driver);

        // 기사 프로필은 기본 정보만 포함 (실시간 데이터는 WebSocket으로 별도 처리)
        return ResponseDriverProfileDto.from(driver, assignedRegions, totalDeliveryCount, completedDeliveryCount, workDuration);
    }

    /**
     * 기사의 근무 시간 계산
     * 
     * @param driver 기사
     * @return 근무 시간
     */
    private Duration calculateWorkDuration(Driver driver) {
        if (driver.getAttendance() != Attendance.WORKING || driver.getWorkTime() == null) {
            return Duration.ZERO;
        }
        return Duration.between(driver.getWorkTime(), LocalDateTime.now());
    }
    
    /**
     * 할당되지 않은 상품 목록 조회 (기사에게 할당하기 전)
     */
    @Transactional(readOnly = true)
    public List<ResponseUnassignedProductDto> getUnassignedProducts() {
        List<Product> products = productRepository.findByDriverIsNullAndShippingStatusIsNullAndIsDeletedFalse();

        if (products.isEmpty()) {
            throw new CustomException(ErrorCode.DELIVERY_NOT_FOUND);
        }

        return products.stream()
                .map(ResponseUnassignedProductDto::from)
                .toList();
    }

}
