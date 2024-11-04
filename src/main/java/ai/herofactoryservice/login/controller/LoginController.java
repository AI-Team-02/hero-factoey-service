package ai.herofactoryservice.login.controller;

import ai.herofactoryservice.login.dto.LoginResponseDto;
import ai.herofactoryservice.login.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class LoginController {
    private final TokenService tokenService;

    @Operation(
            summary = "로그아웃", // API 요약 설명
            description = "액세스 토큰을 무효화하여 사용자를 로그아웃 처리합니다." // API 상세 설명
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        String accessToken = bearerToken.substring(7);
        tokenService.logout(accessToken);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "토큰 재발급",
            description = "만료된 액세스 토큰을 리프레시 토큰을 사용하여 재발급합니다.(리프레시 토큰도 재발급)"
    )
    @PostMapping("/reissue")
    public ResponseEntity<LoginResponseDto> reissueTokens(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("Refresh-Token") String refreshToken
    ) {
        String accessToken = bearerToken.substring(7);
        LoginResponseDto newTokens = tokenService.reissueTokens(accessToken, refreshToken);
        return ResponseEntity.ok(newTokens);
    }
}
