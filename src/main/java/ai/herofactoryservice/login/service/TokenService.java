package ai.herofactoryservice.login.service;

import ai.herofactoryservice.config.security.JwtTokenProvider;
import ai.herofactoryservice.login.dto.LoginResponseDto;
import ai.herofactoryservice.login.dto.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final long REFRESH_TOKEN_VALIDITY = 14 * 24 * 60 * 60; // 14일

    public LoginResponseDto reissueTokens(String accessToken, String refreshToken) {
        // Access Token에서 userId 추출 (만료되어도 추출 가능)
        Long userId = jwtTokenProvider.getUserId(accessToken);
        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;

        // Redis에서 저장된 refresh token 조회
        RefreshToken storedToken = (RefreshToken) redisTemplate.opsForValue().get(refreshTokenKey);
        if (storedToken == null) {
            throw new BadCredentialsException("Refresh token not found");
        }

        // 저장된 refresh token과 요청된 token 비교
        if (!storedToken.getRefreshToken().equals(refreshToken)) {
            // 같지 않으면, 해킹 감지로 토큰 데이터 삭제
            invalidateAllUserTokens(userId);
            throw new BadCredentialsException("Invalid refresh token");
        }

        // 새로운 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken();

        // 새로운 Refresh Token을 Redis에 저장
        RefreshToken newStoredToken = RefreshToken.builder()
                .refreshToken(newRefreshToken)
                .build();
        redisTemplate.opsForValue().set(
                refreshTokenKey,
                newStoredToken,
                REFRESH_TOKEN_VALIDITY,
                TimeUnit.SECONDS
        );

        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public boolean isLogin(String accessToken) {
        Long userId = jwtTokenProvider.getUserId(accessToken);
        RefreshToken storedToken = (RefreshToken) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);

        return storedToken != null ;
    }

    public void logout(String accessToken) {
        Long userId = jwtTokenProvider.getUserId(accessToken);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.info("User {} has been logged out successfully", userId);
    }

    public void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken token = RefreshToken.builder()
                .refreshToken(refreshToken)
                .build();

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                token,
                REFRESH_TOKEN_VALIDITY,
                TimeUnit.SECONDS
        );
    }

    private void invalidateAllUserTokens(Long userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        log.warn("Security breach detected for user {}. All tokens have been invalidated.", userId);
    }
}