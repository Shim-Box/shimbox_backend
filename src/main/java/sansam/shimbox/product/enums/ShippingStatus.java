package sansam.shimbox.product.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import sansam.shimbox.global.common.LabelEnum;

public enum ShippingStatus implements LabelEnum {

    WAITING("배송대기"), STARTED("배송시작"), COMPLETED("배송완료");

    private final String label;

    ShippingStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static ShippingStatus from(String label) {
        return LabelEnum.fromLabel(ShippingStatus.class, label);
    }
}