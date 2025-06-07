package sansam.shimbox.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import sansam.shimbox.auth.domain.CustomUserDetails;
import sansam.shimbox.global.exception.CustomException;
import sansam.shimbox.global.exception.ErrorCode;

import javax.crypto.SecretKey;
import java.util.Calendar;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey secretKey;
    private final int accessTokenExpirationMs;
    private final int refreshTokenExpirationMs;

    // 비밀키 값을 SecretKey 객체로 반환
    public JwtUtil(@Value("${spring.jwt.secret}") String key,
                   @Value("${spring.jwt.accessTokenExpiration}") int accessTokenExpirationMs,
                   @Value("${spring.jwt.refreshTokenExpiration}") int refreshTokenExpirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String createAccessToken(String email, String role, Long userId) {
        return createJwt(email, role, userId,accessTokenExpirationMs);
    }

    public String createRefreshToken(String email, String role, Long userId) {
        return createJwt(email, role, userId, refreshTokenExpirationMs);
    }

    public String createJwt(String email, String role, Long userId, int expirationMs) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, expirationMs);
        return Jwts.builder()
                .claim("email", email)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(cal.getTime())
                .signWith(secretKey)
                .compact();
    }

    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    public String getUsername(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // 토큰 검증 - 토큰 유효기간 비교
    public boolean isExpired(String token) {
        try {
            Date exp = parseClaims(token).getExpiration();
            return new Date().after(exp);
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public int getRefreshTokenExpirationMillis() {
        return refreshTokenExpirationMs;
    }

    public int getAccessTokenExpirationMillis() {
        return accessTokenExpirationMs;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUserId();  // 사용자의 ID 반환
        }

        throw new CustomException(ErrorCode.UNAUTHORIZED);
    }
}