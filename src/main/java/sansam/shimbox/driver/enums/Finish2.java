package sansam.shimbox.driver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import sansam.shimbox.global.common.LabelEnum;

@Schema(description = "설문2", example = "약간 그렇다", enumAsRef = true)
public enum Finish2 implements LabelEnum {

    NOT_AT_ALL("전혀 아니다"), SOMEWHAT_TRUE("약간 그렇다"), VERY_TRUE("매우 그렇다");

    private final String label;

    Finish2(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Finish2 from(String label) { return LabelEnum.fromLabel(Finish2.class, label); }
}