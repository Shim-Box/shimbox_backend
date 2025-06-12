package sansam.shimbox.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseLoginDto {
    private String name;
    private String accessToken;
    private String refreshToken;
}
