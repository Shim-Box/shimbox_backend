package sansam.shimbox.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Schema(name = "RequestUserLoginDto", description = "로그인 요청 DTO")
@Getter
public class RequestUserLoginDto {

    @Schema(description = "사용자 이메일", example = "qwer@naver.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "비밀번호", example = "1234")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}