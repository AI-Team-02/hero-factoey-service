package ai.herofactoryservice.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private final long accessTokenValidityInMilliseconds = 60 * 60 * 1000; // 1시간
    private final long refreshTokenValidityInMilliseconds = 60 * 60 * 24 * 14 * 1000; // 2주
    private SecretKey key;

    @PostConstruct
    protected void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        log.info("JWT key has been initialized");
    }

    public String createAccessToken(Long userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        Date validity = new Date(now.getTime() + (refreshTokenValidityInMilliseconds));
        String refreshTokenId = UUID.randomUUID().toString();

        return Jwts.builder()
                .subject(refreshTokenId)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }


    public Long getUserId(String token) {
        try {
            return Long.parseLong(
                    Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload()
                            .getSubject()
            );
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이어도 Claims 정보는 가져올 수 있음
            return Long.parseLong(e.getClaims().getSubject());
        }
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token", e);
            return false;
        }
    }

}