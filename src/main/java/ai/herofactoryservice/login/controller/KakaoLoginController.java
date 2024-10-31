package ai.herofactoryservice.login.controller;

import ai.herofactoryservice.login.dto.LoginResponseDto;
import ai.herofactoryservice.login.service.KakaoLoginService;
import ai.herofactoryservice.login.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/kakao")
@Slf4j
public class KakaoLoginController {
    private final KakaoLoginService kakaoLoginService;
    private final TokenService tokenService;

    @GetMapping("/login")
    public String kakaoLogin() {
        log.info("Kakao login initiated");
        String kakaoAuthUrl = kakaoLoginService.getKakaoLoginUrl();
        return "redirect:" + kakaoAuthUrl;
    }

    @GetMapping("/callback")
    @ResponseBody
    public LoginResponseDto kakaoCallback(@RequestParam String code) {
        log.info("Kakao callback received with code");
        log.debug("code = {}", code);
        return kakaoLoginService.processKakaoLogin(code);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken,
                                       @RequestHeader("Refresh-Token") String refreshToken) {
        String accessToken = bearerToken.substring(7);
        tokenService.logout(accessToken, refreshToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader("Refresh-Token") String refreshToken
    ) {
        String accessToken = bearerToken.substring(7);
        LoginResponseDto newTokens = tokenService.refreshTokens(accessToken, refreshToken);
        return ResponseEntity.ok(newTokens);
    }
}
