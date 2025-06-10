package sansam.shimbox.product.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import sansam.shimbox.global.common.LabelEnum;

public enum ProductStatus implements LabelEnum {

    BEFORE_SORTING("분류전"), AFTER_SORTING("분류완료");

    private final String label;

    ProductStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static ProductStatus from(String label) {
        return LabelEnum.fromLabel(ProductStatus.class, label);
    }
}
