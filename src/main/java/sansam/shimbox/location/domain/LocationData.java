package sansam.shimbox.location.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sansam.shimbox.driver.enums.Region;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationData {
    
    private double lat; //위도
    private double lng; //경도
    private String capturedAt; //위치 캡처 시간
    private String addressShort; //짧은 주소 (예: "강남구 역삼동")
    private Region region; //지역
    private long timestamp; //마지막 업데이트 시간
    
    // 위치 데이터가 유효한지 확인
    public boolean isValid(long ttlMillis) {
        long now = System.currentTimeMillis();
        return (now - timestamp) <= ttlMillis;
    }
    
    // 위치 데이터가 만료되었는지 확인
    public boolean isExpired(long ttlMillis) {
        return !isValid(ttlMillis);
    }
}
