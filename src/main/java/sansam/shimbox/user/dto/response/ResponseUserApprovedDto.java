package sansam.shimbox.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sansam.shimbox.driver.enums.Attendance;
import sansam.shimbox.driver.enums.ConditionStatus;

@Getter
@AllArgsConstructor
@Builder
public class ResponseUserApprovedDto {

    private Long id;
    private Boolean approvalStatus;
    private String profileImageUrl; // 프로필 이미지 경로
    private String name;
    private Attendance attendance;
    private String residence;
    private String workTime;        // 근무시간: "AM 11:00 - PM 08:00"
    private String deliveryStats;   // 배달 건수: "150 / 250"
    private ConditionStatus conditionStatus;
}