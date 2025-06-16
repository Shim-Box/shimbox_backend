package sansam.shimbox.global.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import sansam.shimbox.driver.enums.ConditionStatus;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String REALTIME_PREFIX = "realtime:driver:";
    private final ObjectMapper objectMapper;

    // RefreshToken 저장 (TTL 설정 포함)
    public void saveRefreshToken(Long userId, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    // RefreshToken 조회
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    // RefreshToken 삭제
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    // 키 존재 여부 확인
    public boolean hasKey(Long userId) {
        return redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + userId);
    }

    // 범용 JSON 저장
    public void saveJson(String key, String json, long ttl, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, json, ttl, unit);
    }

    // 범용 JSON 조회
    public String getJson(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 범용 키 삭제
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    // 키 존재 여부
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 실시간 상태 조회 전용 메서드
    public ConditionStatus getDriverConditionStatus(Long driverId) {
        String key = REALTIME_PREFIX + driverId;
        String json = getJson(key);
        if (json == null) return null;

        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            return ConditionStatus.valueOf((String) map.get("conditionStatus"));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return null;
        }
    }
}