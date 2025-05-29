package sansam.shimbox.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Calendar;
import java.util.Date;

@Component
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

    public String createAccessToken(String email, String role) {
        return createJwt(email, role, refreshTokenExpirationMs);
    }

    public String createRefreshToken(String email, String role) {
        return createJwt(email, role, refreshTokenExpirationMs);
    }

    public String createJwt(String email, String role, int expirationMs) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, expirationMs);
        return Jwts.builder()
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(cal.getTime())
                .signWith(secretKey)
                .compact();
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
}