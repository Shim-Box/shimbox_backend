package sansam.shimbox.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import sansam.shimbox.global.common.LabelEnum;

@Schema(description = "혈압", example = "저혈압", enumAsRef = true)
public enum BloodPressure implements LabelEnum {

    HYPERTENSION("고혈압"), HYPOTENSION("저혈압"), NONE("없음");

    private final String label;

    BloodPressure(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static BloodPressure from(String label) {
        return LabelEnum.fromLabel(BloodPressure.class, label);
    }
}