package sansam.shimbox.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.auth.repository.UserRepository;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.driver.repository.DriverRepository;
import sansam.shimbox.global.redis.RedisService;
import sansam.shimbox.user.dto.request.RequestUserApprovedDto;
import sansam.shimbox.user.dto.request.RequestUserStatusDto;
import sansam.shimbox.user.dto.response.ResponseUserApprovedDto;
import sansam.shimbox.user.dto.response.ResponseUserPendingDto;
import sansam.shimbox.global.common.PagedResponse;
import sansam.shimbox.global.common.RequestPagingDto;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RedisService redisService;

    //가입 대기자 조회
    public PagedResponse<ResponseUserPendingDto> userFindAll(RequestPagingDto pagingDto) {
        Page<User> usersPage = userRepository.findAllByApprovalStatusFalse(pagingDto.toPageable());

        Page<ResponseUserPendingDto> dtoPage = usersPage.map(user ->
                ResponseUserPendingDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .phoneNumber(user.getPhoneNumber())
                        .residence(user.getResidence())
                        .licenseImage(user.getLicenseImage())
                        .approvalStatus(user.getApprovalStatus())
                        .birth(user.getBirth())
                        .career(user.getCareer() != null ? user.getCareer().getLabel() : null)
                        .averageWorking(user.getAverageWorking() != null ? user.getAverageWorking().getLabel() : null)
                        .averageDelivery(user.getAverageDelivery() != null ? user.getAverageDelivery().getLabel() : null)
                        .bloodPressure(user.getBloodPressure() != null ? user.getBloodPressure().getLabel() : null)
                        .role(String.valueOf(user.getRole()))
                        .build()
        );

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

                    return ResponseUserApprovedDto.builder()
                            .userId(user.getId())
                            .driverId(driver.getDriverId())
                            .approvalStatus(user.getApprovalStatus())
                            .profileImageUrl(user.getProfileImage())
                            .averageDelivery(user.getAverageDelivery())
                            .averageWorking(user.getAverageWorking())
                            .career(user.getCareer())
                            .name(user.getName())
                            .attendance(driver.getAttendance())
                            .residence(user.getResidence())
                            .workTime(workTime)
                            .deliveryStats(deliveries + " / " + deliveryTarget)
                            .conditionStatus(realtimeStatus)
                            .build();
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
}
