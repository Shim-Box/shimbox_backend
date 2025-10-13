package sansam.shimbox.driver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.dto.response.ResponseHeartRateTimelineDto;
import sansam.shimbox.driver.repository.DriverRepository;
import sansam.shimbox.global.redis.RedisService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeartRateTimelineService {

    private final RedisService redisService;
    private final DriverRepository driverRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String HEART_RATE_PREFIX = "heartrate:timeline:";
    private static final String HEART_RATE_DAILY_PREFIX = "heartrate:daily:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 심박수 타임라인 데이터 저장 (30분 단위)
     */
    public void saveHeartRateTimeline(Long userId, Integer heartRate, Integer step, String region) {
        try {
            // 기사 정보 조회
            Driver driver = driverRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            LocalDateTime now = LocalDateTime.now();
            String date = now.format(DATE_FORMATTER);
            String timeSlot = getTimeSlot(now); // 30분 단위로 그룹화

            // Redis 키 생성: heartrate:timeline:userId:date:timeSlot
            String key = HEART_RATE_PREFIX + userId + ":" + date + ":" + timeSlot;

            // 데이터 저장
            Map<String, Object> data = Map.of(
                    "userId", userId,
                    "driverName", driver.getUser().getName(),
                    "heartRate", heartRate,
                    "step", step,
                    "recordedAt", now.toString(),
                    "region", region
            );

            String json = objectMapper.writeValueAsString(data);
            redisService.saveJson(key, json, 7, java.util.concurrent.TimeUnit.DAYS); // 7일 보관

            log.debug("Heart rate timeline saved for user {} at {}", userId, now);

        } catch (JsonProcessingException e) {
            log.error("Failed to save heart rate timeline for user {}", userId, e);
        }
    }

    /**
     * 특정 기사의 심박수 타임라인 조회
     */
    public List<ResponseHeartRateTimelineDto> getHeartRateTimeline(Long userId, String date) {
        try {
            List<ResponseHeartRateTimelineDto> timeline = new ArrayList<>();
            
            // 해당 날짜의 모든 시간대 데이터 조회
            String pattern = HEART_RATE_PREFIX + userId + ":" + date + ":*";
            Set<String> keys = redisService.getKeys(pattern);
            
            for (String key : keys) {
                String json = redisService.getJson(key);
                if (json != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = objectMapper.readValue(json, Map.class);
                    timeline.add(ResponseHeartRateTimelineDto.from(
                            Long.valueOf(data.get("userId").toString()),
                            data.get("driverName").toString(),
                            Integer.valueOf(data.get("heartRate").toString()),
                            LocalDateTime.parse(data.get("recordedAt").toString())
                    ));
                }
            }
            
            // 시간순 정렬
            timeline.sort((a, b) -> a.getRecordedAt().compareTo(b.getRecordedAt()));
            
            return timeline;
            
        } catch (Exception e) {
            log.error("Failed to get heart rate timeline for user {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 30분 단위로 시간 슬롯 계산
     */
    private String getTimeSlot(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int slot = (hour * 2) + (minute >= 30 ? 1 : 0);
        return String.format("%02d:%02d", slot / 2, (slot % 2) * 30);
    }
}
