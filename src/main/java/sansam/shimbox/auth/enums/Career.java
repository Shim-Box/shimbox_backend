package sansam.shimbox.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import sansam.shimbox.global.common.LabelEnum;

public enum Career implements LabelEnum {

    BEGINNER("초보자"), EXPERIENCED("경력자"), SKILLED("숙련자");

    private final String label;

    Career(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Career from(String label) {
        return LabelEnum.fromLabel(Career.class, label);
    }
}