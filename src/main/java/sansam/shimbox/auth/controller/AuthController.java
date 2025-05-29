package sansam.shimbox.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sansam.shimbox.auth.dto.request.RequestTokenReissueDto;
import sansam.shimbox.auth.dto.request.RequestUserLoginDto;
import sansam.shimbox.auth.dto.request.RequestUserSaveDto;
import sansam.shimbox.auth.dto.response.TokenDto;
import sansam.shimbox.auth.dto.response.ResponseUserSaveDto;
import sansam.shimbox.auth.service.AuthService;
import sansam.shimbox.global.common.BaseResponse;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.global.swagger.ApiErrorCodeExamples;

@RestController
@RequiredArgsConstructor

@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입 API")
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_REQUEST,
            ErrorCode.EMAIL_ALREADY_EXISTS,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/save")
    public ResponseEntity<BaseResponse<ResponseUserSaveDto>> save(@Valid @RequestBody RequestUserSaveDto dto) {
        ResponseUserSaveDto data = authService.save(dto);
        return ResponseEntity.ok(BaseResponse.success(data,"회원가입 성공", HttpStatus.CREATED));
    }

    @Operation(summary = "로그인 API")
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_REQUEST,
            ErrorCode.INVALID_CREDENTIALS,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenDto>> login(@Valid @RequestBody RequestUserLoginDto dto) {
        TokenDto tokenDto = authService.login(dto);
        return ResponseEntity.ok(BaseResponse.success(tokenDto, "로그인 성공", HttpStatus.OK));
    }

    @Operation(summary = "토큰 재발급 API")
    @ApiErrorCodeExamples({
            ErrorCode.INVALID_REQUEST,
            ErrorCode.TOKEN_EXPIRED,
            ErrorCode.TOKEN_INVALID,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping("/reissue")
    public ResponseEntity<BaseResponse<TokenDto>> reissue(@Valid @RequestBody RequestTokenReissueDto dto) {
        TokenDto newToken = authService.reissueAccessToken(dto);
        return ResponseEntity.ok(BaseResponse.success(newToken, "토큰 재발급 성공", HttpStatus.OK));
    }
}