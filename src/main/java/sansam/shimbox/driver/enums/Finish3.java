package sansam.shimbox.driver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import sansam.shimbox.global.common.LabelEnum;

public enum Finish3 implements LabelEnum {

    LESS_THAN_USUAL("적게"), AS_USUAL("평소대로"), MORE_THAN_USUAL("더 많이");

    private final String label;

    Finish3(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Finish3 from(String label) {
        return LabelEnum.fromLabel(Finish3.class, label);
    }
}
