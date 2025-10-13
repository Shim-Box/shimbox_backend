package sansam.shimbox.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sansam.shimbox.auth.domain.User;

@Getter
@AllArgsConstructor
@Builder
public class ResponseUserPendingDto {

    private Long id;
    private String name;
    private Boolean approvalStatus;
    private String phoneNumber;
    private String residence;
    private String licenseImage;
    private String birth;
    private String career;
    private String averageWorking;
    private String averageDelivery;
    private String bloodPressure;
    private String role;

    public static ResponseUserPendingDto from(User user) {
        return ResponseUserPendingDto.builder()
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
                .build();
    }
}
