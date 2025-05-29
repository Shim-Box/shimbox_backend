package sansam.shimbox.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

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
}