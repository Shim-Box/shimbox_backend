package sansam.shimbox.global.common;

import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;

import java.util.Arrays;

public interface LabelEnum {
    String getLabel();

    static <T extends LabelEnum> T fromLabel(Class<T> enumType, String label) {
        return (T) Arrays.stream(enumType.getEnumConstants())
                .filter(e -> e.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ENUM_VALUE));
    }
}