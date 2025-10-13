package sansam.shimbox.global.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import sansam.shimbox.location.service.LocationRoomService;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper om = new ObjectMapper();
    private final LocationRoomService roomService;

    // 연결 성립 시
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, Object> attrs = session.getAttributes();
        String role = (String) attrs.get("role");
        Long userId = (Long) attrs.get("userId");
        String as = (String) attrs.getOrDefault("as", "web");
        String region = (String) attrs.get("region");

        // LocationRoomService에 세션 등록
        roomService.register(as, session, userId, role, region);
        log.info("[WS-L] connected: user={}, role={}, as={}, region={}", userId, role, as, region);
    }

    // 메세지 수신 시
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode root = om.readTree(message.getPayload());
            String type = root.path("type").asText("");
            JsonNode payload = root.path("payload");
            roomService.handleMobileMessage(session, type, payload);
        } catch (Exception e) {
            log.warn("[WS-L] invalid message", e);
        }
    }

    // 연결 종료 시
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        roomService.unregister(session); // 세션 정리
    }

    // 연결 오류 시
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        roomService.unregister(session);
        try { session.close(CloseStatus.SERVER_ERROR); } catch (IOException ignored) {}
    }
}
