package sansam.shimbox.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import sansam.shimbox.global.common.LabelEnum;

public enum AverageWorking implements LabelEnum {

    HOURS_4_TO_6("4~6시간"), HOURS_6_TO_8("6~8시간"), OVER_8_HOURS("8시간 이상");

    private final String label;

    AverageWorking(String label) { this.label = label; }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static AverageWorking from(String label) {
        return LabelEnum.fromLabel(AverageWorking.class, label);
    }
}