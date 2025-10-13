package sansam.shimbox.location.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import sansam.shimbox.location.dto.response.MessageDriverLocation;
import sansam.shimbox.location.dto.response.MessageDriverHealth;
import sansam.shimbox.location.dto.request.RequestLocationQueryDto;
import sansam.shimbox.location.domain.LocationData;
import sansam.shimbox.driver.enums.Region;
import sansam.shimbox.driver.service.HeartRateTimelineService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LocationRoomService {

    private final ObjectMapper om = new ObjectMapper();
    private final HeartRateTimelineService heartRateTimelineService;

    public LocationRoomService(HeartRateTimelineService heartRateTimelineService) {
        this.heartRateTimelineService = heartRateTimelineService;
        // 주기적으로 오래된 위치 데이터와 죽은 연결 정리
        scheduler.scheduleAtFixedRate(this::cleanupExpiredLocations,
                CLEANUP_INTERVAL_SECONDS, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::cleanupDeadSessions,
                CLEANUP_INTERVAL_SECONDS, CLEANUP_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private static class SessionInfo {
        String as; // mobile|web
        Long userId;
        String role;
        String region;
    }

    private final ConcurrentHashMap<WebSocketSession, SessionInfo> sessions = new ConcurrentHashMap<>(); // 세션 정보 저장
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, LocationData>> realtimeLocationsByRegion = new ConcurrentHashMap<>(); // region별 실시간 위치 데이터
    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, MessageDriverHealth.Payload>> realtimeHealthByRegion = new ConcurrentHashMap<>(); // region별 실시간 건강 데이터
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2); // 스케줄러 (정리 작업용)
    private static final int LOCATION_TTL_SECONDS = 300; // 5분 (위치 데이터 TTL)
    private static final int CLEANUP_INTERVAL_SECONDS = 60; // 1분 (정리 주기)
    

    // 세션 등록
    public void register(String as, WebSocketSession session, Long userId, String role, String region) {
        // 세션 정보 생성
        SessionInfo info = new SessionInfo();
        info.as = as; // 모바일 또는 웹
        info.userId = userId; // jwt에서 추출
        info.role = role; // jwt에서 추출
        info.region = region; // 쿼리 파라미터에서 추출

        // 세션 정보 저장
        sessions.put(session, info);
        
        log.info("[WS-L] Session registered: user={}, role={}, as={}, region={}", userId, role, as, region);
    }

    // websocket 연결 종료시 세션 정리
    public void unregister(WebSocketSession session) {
        SessionInfo info = sessions.remove(session);
        if (info != null) {
            log.info("[WS-L] Session unregistered: user={}, role={}, as={}, region={}", 
                    info.userId, info.role, info.as, info.region);
        }
    }

    // ==================== 메인 메시지 처리 ====================
    
    /**
     * 모바일 앱에서 전송된 실시간 데이터 메시지 처리
     * 위치 데이터와 건강 데이터를 타입에 따라 분기 처리
     */
    public void handleMobileMessage(WebSocketSession session, String messageType, JsonNode payload) {
        // 세션 정보 검증
        SessionInfo sessionInfo = validateMobileSession(session);
        if (sessionInfo == null) return;

        // 메시지 타입에 따른 분기 처리
        switch (messageType) {
            case "location" -> handleLocationData(sessionInfo, payload);
            case "health" -> handleHealthData(sessionInfo, payload);
            default -> log.warn("Unknown message type: {}", messageType);
        }
    }

    // ==================== 세션 검증 ====================
    
    /**
     * 모바일 세션 유효성 검증
     */
    private SessionInfo validateMobileSession(WebSocketSession session) {
        SessionInfo info = sessions.get(session);
        if (info == null || !"mobile".equalsIgnoreCase(info.as)) {
            log.warn("Invalid mobile session");
            return null;
        }
        
        if (info.region == null) {
            log.warn("Region is null for user {}", info.userId);
            return null;
        }
        
        return info;
    }

    // ==================== 위치 데이터 처리 ====================
    
    /**
     * 위치 데이터 처리 및 저장
     */
    private void handleLocationData(SessionInfo sessionInfo, JsonNode payload) {
        // 위치 데이터 추출
        LocationData locationData = extractLocationData(payload, sessionInfo.region);
        
        // 메모리에 저장
        saveLocationData(sessionInfo.userId, sessionInfo.region, locationData);
        
        // 관리자에게 브로드캐스트
        broadcastLocationToAdmins(sessionInfo.userId, locationData, sessionInfo.region);
    }
    
    /**
     * 위치 데이터 추출 및 객체 생성
     */
    private LocationData extractLocationData(JsonNode payload, String region) {
        double lat = payload.path("lat").asDouble();
        double lng = payload.path("lng").asDouble();
        String capturedAt = payload.path("captured_at").asText(Instant.now().toString());
        String addressShort = payload.path("address_short").asText(null);

        return LocationData.builder()
                .lat(lat)
                .lng(lng)
                .capturedAt(capturedAt)
                .addressShort(addressShort)
                .region(Region.from(region))
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    /**
     * 위치 데이터를 메모리에 저장
     */
    private void saveLocationData(Long userId, String region, LocationData locationData) {
        realtimeLocationsByRegion
                .computeIfAbsent(region, k -> new ConcurrentHashMap<>())
                .put(userId, locationData);
        
        log.debug("Location data saved for user {} in region {}", userId, region);
    }

    // ==================== 건강 데이터 처리 ====================
    
    /**
     * 건강 데이터 처리 및 저장
     */
    private void handleHealthData(SessionInfo sessionInfo, JsonNode payload) {
        // 건강 데이터 추출
        MessageDriverHealth.Payload healthPayload = extractHealthData(payload, sessionInfo.userId);
        
        // 메모리에 저장
        saveHealthData(sessionInfo.userId, sessionInfo.region, healthPayload);
        
        // 관리자에게 브로드캐스트
        broadcastHealthToAdmins(sessionInfo.userId, healthPayload, sessionInfo.region);
    }
    
    /**
     * 건강 데이터 추출 및 객체 생성
     */
    private MessageDriverHealth.Payload extractHealthData(JsonNode payload, Long userId) {
        Integer step = payload.path("step").asInt();
        Integer heartRate = payload.path("heartRate").asInt();
        String capturedAt = payload.path("captured_at").asText(Instant.now().toString());

        return new MessageDriverHealth.Payload(
                String.valueOf(userId),
                step,
                heartRate,
                capturedAt
        );
    }
    
    /**
     * 건강 데이터를 메모리에 저장
     */
    private void saveHealthData(Long userId, String region, MessageDriverHealth.Payload healthPayload) {
        realtimeHealthByRegion
                .computeIfAbsent(region, k -> new ConcurrentHashMap<>())
                .put(userId, healthPayload);

        // Redis에 심박수 타임라인 저장 (30분 단위)
        heartRateTimelineService.saveHeartRateTimeline(
                userId, 
                healthPayload.heartRate(), 
                healthPayload.step(), 
                region
        );

        log.debug("Health data saved for user {} in region {}", userId, region);
    }


    // 특정 기사의 현재 위치 조회
    public LocationData getDriverLocation(Long userId) {
        for (ConcurrentHashMap<Long, LocationData> regionLocations : realtimeLocationsByRegion.values()) {
            LocationData location = regionLocations.get(userId);
            if (location != null) {
                return location;
            }
        }
        return null;
    }

    // 실시간 위치 목록 조회
    public List<LocationData> getRealtimeLocations(RequestLocationQueryDto queryDto) {
        // 지역 필터링이 있는 경우
        if (queryDto != null && queryDto.getRegion() != null) {
            String regionLabel = queryDto.getRegion().getLabel();
            ConcurrentHashMap<Long, LocationData> regionLocations = realtimeLocationsByRegion.get(regionLabel);
            if (regionLocations == null) return List.of();
            return regionLocations.values().stream().toList();
        } else {
            // 모든 지역의 기사 위치 조회
            return realtimeLocationsByRegion.values().stream()
                    .flatMap(regionLocations -> regionLocations.values().stream())
                    .toList();
        }
    }

    // 실시간 건강 데이터 조회 (지역별 필터링)
    public List<MessageDriverHealth.Payload> getRealtimeHealth(String region) {
        if (region != null && !region.isEmpty()) {
            // 특정 지역의 건강 데이터만 조회
            ConcurrentHashMap<Long, MessageDriverHealth.Payload> regionHealth = realtimeHealthByRegion.get(region);
            if (regionHealth != null) {
                return List.copyOf(regionHealth.values());
            }
            return List.of();
        } else {
            // 모든 지역의 건강 데이터 조회
            return realtimeHealthByRegion.values().stream()
                    .flatMap(regionHealth -> regionHealth.values().stream())
                    .toList();
        }
    }

    // 특정 기사의 현재 건강 데이터 조회
    public MessageDriverHealth.Payload getDriverHealth(Long userId) {
        for (ConcurrentHashMap<Long, MessageDriverHealth.Payload> regionHealth : realtimeHealthByRegion.values()) {
            MessageDriverHealth.Payload healthData = regionHealth.get(userId);
            if (healthData != null) {
                return healthData;
            }
        }
        return null;
    }

    // ==================== 브로드캐스트 ====================
    
    /**
     * 위치 데이터를 관리자들에게 브로드캐스트
     */
    private void broadcastLocationToAdmins(Long userId, LocationData locationData, String region) {
        MessageDriverLocation.Payload payload = new MessageDriverLocation.Payload(
                String.valueOf(userId),
                region,
                locationData.getLat(),
                locationData.getLng(),
                locationData.getCapturedAt(),
                locationData.getAddressShort()
        );
        
        MessageDriverLocation message = new MessageDriverLocation("location", payload);
        broadcastToAdmins(message, region);
    }

    /**
     * 건강 데이터를 관리자들에게 브로드캐스트
     */
    private void broadcastHealthToAdmins(Long userId, MessageDriverHealth.Payload healthPayload, String region) {
        MessageDriverHealth message = new MessageDriverHealth("health", healthPayload);
        broadcastToAdmins(message, region);
    }
    
    /**
     * 관리자들에게 메시지 브로드캐스트 (공통 로직)
     */
    private void broadcastToAdmins(Object message, String region) {
        String json;
        try { 
            json = om.writeValueAsString(message); 
        } catch (Exception e) { 
            log.error("Failed to serialize message", e);
            return; 
        }

        // 지역별 필터링하여 관리자에게 브로드캐스트
        for (Map.Entry<WebSocketSession, SessionInfo> entry : sessions.entrySet()) {
            WebSocketSession session = entry.getKey();
            SessionInfo info = entry.getValue();
            
            if (isValidAdminSession(info) && shouldSendToAdmin(info, region)) {
                sendMessageToSession(session, json);
            }
        }
    }
    
    /**
     * 관리자 세션 유효성 검증
     */
    private boolean isValidAdminSession(SessionInfo info) {
        return "ADMIN".equals(info.role) && "web".equalsIgnoreCase(info.as);
    }
    
    /**
     * 관리자에게 메시지 전송 여부 결정
     */
    private boolean shouldSendToAdmin(SessionInfo info, String region) {
        return (info.region == null || info.region.isEmpty()) || info.region.equals(region);
    }
    
    /**
     * 세션에 메시지 전송
     */
    private void sendMessageToSession(WebSocketSession session, String json) {
        if (!session.isOpen()) return;
        
        try { 
            session.sendMessage(new TextMessage(json)); 
            log.debug("Message sent to admin session");
        } catch (Exception e) {
            log.error("Failed to send message to session", e);
        }
    }
    
    // ==================== 데이터 정리 ====================
    
    /**
     * 오래된 데이터 정리 (위치: 5분, 건강: 1시간)
     */
    private void cleanupExpiredLocations() {
        cleanupExpiredLocationData();
        cleanupExpiredHealthData();
    }
    
    /**
     * 오래된 위치 데이터 정리
     */
    private void cleanupExpiredLocationData() {
        long locationTtlMillis = LOCATION_TTL_SECONDS * 1000L;
        
        realtimeLocationsByRegion.forEach((region, regionLocations) -> {
            regionLocations.entrySet().removeIf(entry -> {
                LocationData data = entry.getValue();
                boolean expired = data.isExpired(locationTtlMillis);
                if (expired) {
                    log.debug("Removed expired location for user {} in region {}", entry.getKey(), region);
                }
                return expired;
            });
        });
    }
    
    /**
     * 오래된 건강 데이터 정리
     */
    private void cleanupExpiredHealthData() {
        long healthTtlMillis = 60 * 60 * 1000L; // 1시간
        
        realtimeHealthByRegion.forEach((region, regionHealth) -> {
            regionHealth.entrySet().removeIf(entry -> {
                MessageDriverHealth.Payload healthData = entry.getValue();
                long currentTime = System.currentTimeMillis();
                
                // capturedAt 시간을 밀리초로 변환
                long healthTime = Instant.parse(healthData.capturedAt()).toEpochMilli();
                boolean expired = (currentTime - healthTime) > healthTtlMillis;
                
                if (expired) {
                    log.debug("Removed expired health data for user {} in region {}", entry.getKey(), region);
                }
                return expired;
            });
        });
    }
    
    // 죽은 WebSocket 세션 정리
    private void cleanupDeadSessions() {
        sessions.entrySet().removeIf(entry -> {
            WebSocketSession session = entry.getKey();
            boolean isDead = !session.isOpen();
            if (isDead) {
                SessionInfo info = entry.getValue();
                log.debug("Removed dead session for user {} in region {}", info.userId, info.region);
            }
            return isDead;
        });
    }
}
