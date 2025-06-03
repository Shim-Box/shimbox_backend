package sansam.shimbox.auth.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ResponseUserFindAllDto {

    private Long id;
    private String email;
    private String name;
    private String residence;
    private Boolean approvalStatus;
}
