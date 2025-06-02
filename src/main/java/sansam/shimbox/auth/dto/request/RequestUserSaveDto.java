package sansam.shimbox.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Schema(name = "RequestUserSaveDto", description = "회원가입 요청 DTO")
@Getter
public class RequestUserSaveDto {

    @Schema(description = "이메일", example = "qwer@gmail.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "비밀번호", example = "1234")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하여야 합니다.")
    //@Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String password;

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 10, message = "이름은 10자 이내여야 합니다.")
    private String name;

    @Schema(description = "주민등록번호 앞 7자리", example = "9001011")
    @NotBlank(message = "주민번호는 필수입니다.")
    @Pattern(regexp = "\\d{7}", message = "주민번호는 숫자 7자리여야 합니다.")
    private String identificationNumber;

    @Schema(description = "전화번호", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "\\d{11}", message = "전화번호는 숫자 11자리여야 합니다.")
    private String phoneNumber;

    @Schema(description = "거주지", example = "서울특별시 구로구")
    @NotBlank(message = "거주지는 필수입니다.")
    private String residence;

    @Schema(description = "키 (cm)", example = "175")
    @NotNull(message = "키는 필수입니다.")
    private Integer height;

    @Schema(description = "몸무게 (kg)", example = "70")
    @NotNull(message = "몸무게는 필수입니다.")
    private Integer weight;

    @Schema(description = "운전면허 이미지 URL", example = "https://s3.bucket.com/license.jpg")
    @NotBlank(message = "면허 이미지는 필수입니다.")
    private String licenseImage;


    @Schema(description = "경력", example = "경력자", allowableValues = {"초보자", "경력자", "숙련자"})
    @NotBlank(message = "경력은 필수입니다.")
    private String career;

    @Schema(description = "평균 근무 시간", example = "4~6시간", allowableValues = {"4~6시간", "6~8시간", "8시간 이상"})
    private String averageWorking;

    @Schema(description = "평균 배달 건수", example = "201~300건", allowableValues = {"100건 이하", "101~200건", "201~300건", "300건 이상"})
    private String averageDelivery;

    @Schema(description = "혈압 상태", example = "없음", allowableValues = {"고혈압", "저혈압", "없음"})
    @NotBlank(message = "건강 상태는 필수입니다.")
    private String bloodPressure;
}
