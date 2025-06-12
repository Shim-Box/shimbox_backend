package sansam.shimbox.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ResponseUserPendingDto {

    private Long id;
    private String name;
    private String phoneNumber;
    private String residence;
    private String licenseImage;
    private Boolean approvalStatus;
    private String birth;
    private String career;
    private String averageWorking;
    private String averageDelivery;
    private String bloodPressure;
    private String role;
}
