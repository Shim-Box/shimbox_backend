package sansam.shimbox.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sansam.shimbox.auth.domain.User;
import sansam.shimbox.auth.enums.AverageDelivery;
import sansam.shimbox.auth.enums.AverageWorking;
import sansam.shimbox.auth.enums.Career;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.enums.ConditionStatus;

@Getter
@AllArgsConstructor
@Builder
public class ResponseUserApprovedDto {

    private Long userId;
    private Long driverId;
    private String name;
    private Boolean approvalStatus;
    private String profileImageUrl;
    private Career career;
    private AverageWorking averageWorking;
    private AverageDelivery averageDelivery;
    private Attendance attendance;
    private String residence;
    private String workTime;        // 근무시간: "AM 11:00 - PM 08:00"
    private String deliveryStats;   // 배달 건수: "150 / 250"
    private ConditionStatus conditionStatus;

    public static ResponseUserApprovedDto from(User user, Driver driver, String workTime, 
            int deliveries, int deliveryTarget, ConditionStatus realtimeStatus) {
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
    }
}