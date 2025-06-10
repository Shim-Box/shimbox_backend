package sansam.shimbox.driver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import sansam.shimbox.global.common.LabelEnum;

@Schema(description = "건강 상태", example = "좋음", enumAsRef = true)
public enum ConditionStatus implements LabelEnum {

    DANGER("위험"), ANXIOUS("불안"), GOOD("좋음");

    private final String label;

    ConditionStatus(String label) {
        this.label = label;
    }

    @Override
    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static ConditionStatus from(String label) {
        return LabelEnum.fromLabel(ConditionStatus.class, label);
    }
}