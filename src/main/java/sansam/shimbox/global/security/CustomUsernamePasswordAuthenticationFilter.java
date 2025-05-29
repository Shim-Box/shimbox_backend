package sansam.shimbox.global.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;
import sansam.shimbox.auth.domain.CustomUserDetails;
import sansam.shimbox.auth.dto.response.TokenDto;
import sansam.shimbox.auth.dto.request.RequestUserLoginDto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    ObjectMapper objectMapper = new ObjectMapper();

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public CustomUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // request body GET
        ServletInputStream inputStream;
        String requestBody;
        try {
            inputStream = request.getInputStream();
            requestBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Json data parsing
        RequestUserLoginDto requestUserLoginDto;
        try {
            requestUserLoginDto = objectMapper.readValue(requestBody, RequestUserLoginDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(requestUserLoginDto.getEmail(), requestUserLoginDto.getPassword(), null);
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        // 토큰 생성
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();
        String accessToken = jwtUtil.createAccessToken(userDetails.getUser().getEmail(), userDetails.getUser().getRole().name());
        String refreshToken = jwtUtil.createRefreshToken(userDetails.getUser().getEmail(), userDetails.getUser().getRole().name());

        // 토큰을 JSON 형태로 변경
        TokenDto tokenDto = new TokenDto(accessToken, refreshToken);
        String jsonResponse = objectMapper.writeValueAsString(tokenDto);

        // JSON 타입 객체 응답
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
    }
}
