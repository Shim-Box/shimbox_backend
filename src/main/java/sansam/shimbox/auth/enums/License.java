package sansam.shimbox.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import sansam.shimbox.global.common.LabelEnum;

public enum License implements LabelEnum {

    TYPE_1("1종"), TYPE_2("2종"), LARGE_TYPE_1("1종 대형");

    private final String label;

    License(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static License from(String label) {
        return LabelEnum.fromLabel(License.class, label);
    }
}