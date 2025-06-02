package sansam.shimbox.driver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import sansam.shimbox.global.common.LabelEnum;

public enum Attendance implements LabelEnum {

    BEFORE_WORK("출근전"), WORKING("출근"), OFF_WORK("퇴근");

    private final String label;

    Attendance(String label) { this.label = label; }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Attendance from(String label) {
        return LabelEnum.fromLabel(Attendance.class, label);
    }
}