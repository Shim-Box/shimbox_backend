package sansam.shimbox.driver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import sansam.shimbox.global.common.LabelEnum;

@Schema(description = "배송 지역", example = "구로구", enumAsRef = true)
public enum Region implements LabelEnum {

    GURO("구로구"),
    YANGCHEON("양천구"),
    GANGSEO("강서구"),
    YEONGDEUNGPO("영등포구"),
    GEUMCHEON("금천구"),
    DONGJAK("동작구"),
    SEONGBUK("성북구"),
    GANGBUK("강북구"),  
    DONGDAEMUN("동대문구"),
    SEONGDONG("성동구"),
    JONGNO("종로구"),
    JUNG("중구");

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