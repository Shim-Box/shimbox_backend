package sansam.shimbox.driver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import sansam.shimbox.global.common.LabelEnum;

@Schema(description = "배송 지역", example = "신도림동", enumAsRef = true)
public enum Region implements LabelEnum {

    신도림동("신도림동"),
    구로동("구로동"),
    가리봉동("가리봉동"),
    고척동("고척동"),
    개봉동("개봉동"),
    오류동("오류동"),
    궁동("궁동"),
    온수동("온수동"),
    천왕동("천왕동"),
    항동("항동"),
    구로1동("구로1동"),
    구로2동("구로2동"),
    구로3동("구로3동"),
    구로4동("구로4동"),
    구로5동("구로5동"),
    고척1동("고척1동"),
    고척2동("고척2동"),
    개봉1동("개봉1동"),
    개봉2동("개봉2동"),
    개봉3동("개봉3동"),
    오류1동("오류1동"),
    오류2동("오류2동"),
    수궁동("수궁동");

    private final String label;

    Region(String label) {
        this.label = label;
    }

    @Override
    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Region from(String label) {
        return LabelEnum.fromLabel(Region.class, label);
    }
}