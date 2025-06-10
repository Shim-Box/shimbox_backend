package sansam.shimbox.driver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import sansam.shimbox.global.common.LabelEnum;

@Schema(description = "설문1", example = "비슷했다", enumAsRef = true)
public enum Finish1 implements LabelEnum {

    LESS("적었다"), SIMILAR("비슷했다"), MORE("많았다");

    private final String label;

    Finish1(String label) { this.label = label; }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Finish1 from(String label) {
        return LabelEnum.fromLabel(Finish1.class, label);
    }
}