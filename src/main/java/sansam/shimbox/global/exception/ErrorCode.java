package sansam.shimbox.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 ENUM 값입니다."),
    CAREER_DETAILS_SHOULD_BE_NULL(HttpStatus.BAD_REQUEST, "초보자의 근무/배송 경력은 입력할 수 없습니다."),
    CAREER_DETAILS_REQUIRED(HttpStatus.BAD_REQUEST, "경력자일 경우 근무/배송 경력은 필수입니다."),
    INVALID_ATTENDANCE_STATUS(HttpStatus.BAD_REQUEST, "올바르지 않은 출근 상태입니다."),
    ALREADY_IN_SAME_STATUS(HttpStatus.BAD_REQUEST, "이미 같은 상태입니다."),
    INVALID_ATTENDANCE_TRANSITION(HttpStatus.BAD_REQUEST,"현재 상태에서는 해당 상태로 전환할 수 없습니다."),
    INVALID_ATTENDANCE_CHANGE(HttpStatus.BAD_REQUEST, "출근/퇴근 상태 전환이 유효하지 않습니다."),
    INVALID_SHIPPING_TRANSITION(HttpStatus.BAD_REQUEST, "배송 상태 전환이 유효하지 않습니다."),
    ALREADY_UPLOADED_IMAGE(HttpStatus.BAD_REQUEST, "이미 이미지가 업로드된 상품입니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "해당 요청에 대한 권한이 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USERS_NOT_FOUND(HttpStatus.NOT_FOUND, "일부 유저를 찾을 수 없습니다."),
    DRIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "기사 정보를 찾을 수 없습니다."),
    HEALTH_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 드라이버의 건강 기록이 존재하지 않습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "배정받은 상품이 없습니다."),
    SHIPP_NOT_FOUND(HttpStatus.NOT_FOUND, "배송 정보가 존재하지 않습니다."),

    // 409 Conflict
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}