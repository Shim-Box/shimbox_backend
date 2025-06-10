package sansam.shimbox.auth.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import sansam.shimbox.global.common.LabelEnum;

@Schema(description = "평균 건수", example = "100건 이하", enumAsRef = true)
public enum AverageDelivery implements LabelEnum {

    UNDER_100("100건 이하"), BETWEEN_101_AND_200("101~200건"), BETWEEN_201_AND_300("201~300건"), OVER_300("300건 이상");

    private final String label;

    AverageDelivery(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static AverageDelivery from(String label) {
        return LabelEnum.fromLabel(AverageDelivery.class, label);
    }
}