package sansam.shimbox.global.common;


import java.util.stream.Stream;

public interface LabelEnum {
    String getLabel();

    static <T extends Enum<T> & LabelEnum> T fromLabel(Class<T> enumType, String label) {
        return Stream.of(enumType.getEnumConstants())
                .filter(e -> e.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown label: " + label));
    }
}