package sansam.shimbox.global.common;

import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;

import java.util.Arrays;
import java.util.List;

public interface LabelEnum {
    String getLabel();

    @SuppressWarnings("unchecked")
    static <T extends LabelEnum> T fromLabel(Class<T> enumType, String label) {
        return (T) Arrays.stream(enumType.getEnumConstants())
                .filter(e -> e.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new CustomException(
                        ErrorCode.INVALID_ENUM_VALUE,
                        enumType.getSimpleName(),
                        label,
                        labels(enumType)
                ));
    }

    static <T extends LabelEnum> List<String> labels(Class<T> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(LabelEnum::getLabel)
                .toList();
    }
}