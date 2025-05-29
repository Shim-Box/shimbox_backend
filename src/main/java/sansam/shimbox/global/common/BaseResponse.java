package sansam.shimbox.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseResponse<T> {

    private final T data;
    private final String message;
    private final int statusCode;

    public static <T> BaseResponse<T> success(T data, String message, HttpStatus status) {
        return new BaseResponse<>(data, message, status.value());
    }

    public static <T> BaseResponse<T> error(String message, HttpStatus status) {
        return new BaseResponse<>(null, message, status.value());
    }
}