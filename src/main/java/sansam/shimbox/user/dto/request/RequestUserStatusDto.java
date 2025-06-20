package sansam.shimbox.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class RequestUserStatusDto {

    @NotEmpty(message = "유저 ID 리스트는 필수입니다.")
    private List<Long> userIds;
}
