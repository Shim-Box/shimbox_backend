package sansam.shimbox.global.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private String field;
    private String invalidValue;
    private List<String> allowedValues;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, String field, String invalidValue, List<String> allowedValues) {
        super(String.format("[%s] 잘못된 값: '%s'. 허용값: %s", field, invalidValue, allowedValues));
        this.errorCode = errorCode;
        this.field = field;
        this.invalidValue = invalidValue;
        this.allowedValues = allowedValues;
    }
}
