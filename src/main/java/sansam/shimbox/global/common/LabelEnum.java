package sansam.shimbox.global.common;


import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;

import java.util.stream.Stream;

public interface LabelEnum {
    String getLabel();

    static <T extends Enum<T> & LabelEnum> T fromLabel(Class<T> enumType, String label) {
        return Stream.of(enumType.getEnumConstants())
                .filter(e -> e.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ENUM_VALUE));
    }
}