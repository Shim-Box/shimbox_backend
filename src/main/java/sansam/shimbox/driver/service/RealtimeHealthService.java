package sansam.shimbox.driver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sansam.shimbox.driver.domain.Driver;
import sansam.shimbox.driver.dto.request.RequestRealTimeHealthSaveDto;
import sansam.shimbox.driver.dto.response.ResponseRealTimeHealthDto;
import sansam.shimbox.driver.enums.ConditionStatus;
import sansam.shimbox.driver.repository.DriverRepository;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.global.redis.RedisService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RealtimeHealthService {

    private final DriverRepository driverRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    private static final String REDIS_PREFIX = "realtime:driver:";

    //실시간 기사 건강 데이터 저장
    public void realTimeHealthSave(Long userId, RequestRealTimeHealthSaveDto dto) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        String key = REDIS_PREFIX + driver.getDriverId();
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "driverId", driver.getDriverId(),
                    "step", dto.getStep(),
                    "heartRate", dto.getHeartRate(),
                    "conditionStatus", dto.getConditionStatus() != null ? dto.getConditionStatus().name() : ConditionStatus.GOOD.name(),
                    "modifiedDate", LocalDateTime.now().toString()
            ));
            redisService.saveJson(key, json, 12, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.REDIS_SAVE_FAILED);
        }
    }

    //실시간 기사 건강 데이터 조회
    public ResponseRealTimeHealthDto getRealtimeHealth(Long userId) {
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));

        String key = REDIS_PREFIX + driver.getDriverId();
        String json = redisService.getJson(key);
        if (json == null) {
            throw new CustomException(ErrorCode.HEALTH_RECORD_NOT_FOUND);
        }

        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            return ResponseRealTimeHealthDto.builder()
                    .driverId(driver.getDriverId())
                    .step((Integer) map.get("step"))
                    .heartRate((Integer) map.get("heartRate"))
                    .conditionStatus(ConditionStatus.valueOf((String) map.get("conditionStatus")))
                    .modifiedDate(LocalDateTime.parse((String) map.get("modifiedDate")))
                    .build();
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.REDIS_PARSE_FAILED);
        }
    }

    //실시간 기사 건강 데이터 삭제 (퇴근)
    public void deleteRealtimeHealth(Long driverId) {
        Driver driver = driverRepository.findByUserId(driverId)
                .orElseThrow(() -> new CustomException(ErrorCode.DRIVER_NOT_FOUND));
        String key = REDIS_PREFIX + driver.getDriverId();
        Boolean deleted = redisService.delete(key);

        if (Boolean.FALSE.equals(deleted)) {
            throw new CustomException(ErrorCode.REDIS_DELETE_FAILED);
        }
    }

    //실시간 기사 건강 데이터 전체 조회
    public List<ResponseRealTimeHealthDto> getAllRealtimeHealth() {
        List<Driver> allDrivers = driverRepository.findAll();
        return allDrivers.stream()
                .map(driver -> {
                    String key = REDIS_PREFIX + driver.getDriverId();
                    String json = redisService.getJson(key);
                    if (json == null) return null;
                    try {
                        Map<String, Object> map = objectMapper.readValue(json, Map.class);
                        return ResponseRealTimeHealthDto.builder()
                                .driverId(driver.getDriverId())
                                .step((Integer) map.get("step"))
                                .heartRate((Integer) map.get("heartRate"))
                                .conditionStatus(ConditionStatus.valueOf((String) map.get("conditionStatus")))
                                .modifiedDate(LocalDateTime.parse((String) map.get("modifiedDate")))
                                .build();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
