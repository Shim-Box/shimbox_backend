package sansam.shimbox.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import sansam.shimbox.global.security.JwtHandshakeInterceptor;
import sansam.shimbox.global.security.JwtPrincipalHandshakeHandler;
import sansam.shimbox.global.security.JwtUtil;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler locationWebSocketHandler;
    private final JwtUtil jwtUtil;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(locationWebSocketHandler, "/ws/location")
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil)) //jwt 검증
                .setHandshakeHandler(new JwtPrincipalHandshakeHandler()) //Principal 설정
                .setAllowedOrigins("*");
    }

}