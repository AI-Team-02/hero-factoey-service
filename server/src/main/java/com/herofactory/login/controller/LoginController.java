package com.herofactory.login.controller;

import com.herofactory.login.dto.LoginResponseDto;
import com.herofactory.login.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "인증 관련 API - 토큰 재발급 & 로그아웃")
public class LoginController {
    private final TokenService tokenService;

    @Operation(
            summary = "로그아웃", // API 요약 설명
            description = "액세스 토큰을 무효화하여 사용자를 로그아웃 처리합니다." // API 상세 설명
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(
                    description = "Bearer 액세스 토큰",
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    required = true
            ) @RequestHeader("Authorization") String bearerToken) {
        String accessToken = bearerToken.substring(7);
        tokenService.logout(accessToken);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "토큰 재발급",
            description = "만료된 액세스 토큰을 리프레시 토큰을 사용하여 재발급합니다.  \n\n" +
                    "모든 API 요청에서 액세스 토큰이 만료된 경우 다음과 같은 응답이 반환됩니다:  \n" +
                    "```json\n" +
                    "{\n" +
                    "    \"status\": 401,\n" +
                    "    \"message\": \"Access token has expired\",\n" +
                    "    \"code\": \"token-expired\"\n" +
                    "}\n" +
                    "```  \n\n" +
                    "프론트엔드에서는 이 응답(401 상태 코드 + code: 'token-expired')을 받으면 " +
                    "반드시 이 토큰 재발급 API를 호출하여 새로운 액세스 토큰을 발급받아야 합니다.  \n\n" +
                    "재발급 성공 시 새로운 액세스 토큰과 리프레시 토큰이 모두 발급됩니다. \n\n" +
                    "실패 시 401 상태코드가 반환됩니다."
    )
    @PostMapping("/reissue")
    public ResponseEntity<LoginResponseDto> reissueTokens(
            @Parameter(
                    description = "만료된 Bearer 액세스 토큰",
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    required = true
            ) @RequestHeader("Authorization") String bearerToken,
            @Parameter(
                    description = "리프레시 토큰",
                    example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    required = true
            ) @RequestHeader("Refresh-Token") String refreshToken
    ) {
        String accessToken = bearerToken.substring(7);
        LoginResponseDto newTokens = tokenService.reissueTokens(accessToken, refreshToken);
        return ResponseEntity.ok(newTokens);
    }
}
