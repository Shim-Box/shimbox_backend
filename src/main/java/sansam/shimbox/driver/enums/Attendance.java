package sansam.shimbox.driver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import sansam.shimbox.global.common.LabelEnum;

@Schema(description = "근무 상태", example = "출근", enumAsRef = true)
public enum Attendance implements LabelEnum {

    BEFORE_WORK("출근전"), WORKING("출근"), OFF_WORK("퇴근");

    private final String label;

    Attendance(String label) { this.label = label; }

    @Override
    @JsonValue // JSON 직렬화 시 사용
    public String getLabel() {
        return label;
    }
}