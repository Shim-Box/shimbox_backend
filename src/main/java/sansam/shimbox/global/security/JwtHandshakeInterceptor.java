package sansam.shimbox.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import sansam.shimbox.auth.enums.Role;

import java.util.Map;

@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        // 토큰 검증
        String token = resolveToken(request);

        if (!StringUtils.hasText(token) || jwtUtil.isExpired(token)) {
            return false; // 거절
        }

        String email = jwtUtil.getUsername(token);
        String roleStr = jwtUtil.getRole(token);
        Long userId = jwtUtil.getUserId(token);
        Role role = Role.valueOf(roleStr);

        // 핸들러에서 쓰도록 세션 attributes에 저장
        attributes.put("userId", userId);
        attributes.put("email", email);
        attributes.put("role", role.name());

        // as=mobile|web 모바일 또는 웹
        String as = resolveQueryParam(request, "as");
        if (as != null) {
            attributes.put("as", as);
        }

        // region 파라미터 추출
        String region = resolveQueryParam(request, "region");
        if (region != null) {
            attributes.put("region", region);
        }

        // Principal 생성용 (HandshakeHandler에서 사용)
        attributes.put("principalName", String.valueOf(userId));

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}

    private String resolveToken(ServerHttpRequest request) {
        // 쿼리파라미터 ?token=xxxxx 에서 토큰을 읽는다
        String query = request.getURI().getQuery();
        if (StringUtils.hasText(query)) {
            for (String pair : query.split("&")) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    String k = pair.substring(0, idx);
                    String v = pair.substring(idx + 1);
                    if ("token".equalsIgnoreCase(k) && StringUtils.hasText(v)) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    private String resolveQueryParam(ServerHttpRequest request, String key) {
        String query = request.getURI().getQuery();
        if (!StringUtils.hasText(query)) return null;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String k = pair.substring(0, idx);
                String v = pair.substring(idx + 1);
                if (key.equalsIgnoreCase(k) && StringUtils.hasText(v)) {
                    return v;
                }
            }
        }
        return null;
    }
}
