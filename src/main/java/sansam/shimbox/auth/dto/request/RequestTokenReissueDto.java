package sansam.shimbox.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Schema(name = "RequestTokenReissueDto", description = "토큰 재발급 요청 DTO")
@Getter
public class RequestTokenReissueDto {

    @Schema(description = "사용자 ID", example = "1")
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
}