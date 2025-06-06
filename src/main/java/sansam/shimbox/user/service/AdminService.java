package sansam.shimbox.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.auth.repository.UserRepository;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.driver.repository.DriverRepository;
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

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    //신청 유저 조회
    public PagedResponse<ResponseUserPendingDto> userFindAll(RequestPagingDto pagingDto) {
        Page<User> usersPage = userRepository.findAllByApprovalStatusFalse(pagingDto.toPageable());

        Page<ResponseUserPendingDto> dtoPage = usersPage.map(user ->
                ResponseUserPendingDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .phoneNumber(user.getPhoneNumber())
                        .residence(user.getResidence())
                        .approvalStatus(user.getApprovalStatus())
                        .birth(user.getBirth())
                        .career(user.getCareer() != null ? user.getCareer().getLabel() : null)
                        .averageWorking(user.getAverageWorking() != null ? user.getAverageWorking().getLabel() : null)
                        .averageDelivery(user.getAverageDelivery() != null ? user.getAverageDelivery().getLabel() : null)
                        .bloodPressure(user.getBloodPressure() != null ? user.getBloodPressure().getLabel() : null)
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
                            .workStartTime(null)
                            .isDeleted(false)
                            .build();

                    driverRepository.save(driver);
                }
            }
        }

        return approvedIds;
    }

    //승인된 유저 조회
    public PagedResponse<ResponseUserApprovedDto> approvedUserFindAll(RequestUserApprovedDto dto) {
        Page<User> usersPage = userRepository.findApprovedUsersWithFilter(
                dto.getResidence(),
                dto.getAttendance(),
                dto.getConditionStatus(),
                dto.toPageable()
        );

        Page<ResponseUserApprovedDto> dtoPage = usersPage.map(user -> {
            Driver driver = user.getDriver();
            int deliveries = driver != null && driver.getDriverRealtime() != null && driver.getDriverRealtime().getRealTimeDeliveryCount() != null
                    ? driver.getDriverRealtime().getRealTimeDeliveryCount() : 0;
            int deliveryTarget = driver != null && driver.getShipps() != null ? driver.getShipps().size() : 0;

            String workTime = "-";
            if (driver != null && driver.getDriverRealtime() != null && driver.getDriverRealtime().getRealTimeWorkMinutes() != null
                    && driver.getWorkStartTime() != null) {
                LocalDateTime startTime = driver.getWorkStartTime();
                LocalDateTime endTime = startTime.plusMinutes(driver.getDriverRealtime().getRealTimeWorkMinutes());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a hh:mm").withLocale(java.util.Locale.KOREA);
                workTime = startTime.format(formatter) + " - " + endTime.format(formatter);
            }

            return ResponseUserApprovedDto.builder()
                    .id(user.getId())
                    .approvalStatus(user.getApprovalStatus())
                    .profileImageUrl(user.getProfileImage())
                    .name(user.getName())
                    .attendance(driver != null ? driver.getAttendance() : null)
                    .residence(user.getResidence())
                    .workTime(workTime)
                    .deliveryStats(deliveries + " / " + deliveryTarget)
                    .conditionStatus(driver != null && driver.getDriverRealtime() != null ? driver.getDriverRealtime().getRealTimeConditionStatus() : null)
                    .build();
        });

        return new PagedResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber() + 1,
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages()
        );
    }
}
