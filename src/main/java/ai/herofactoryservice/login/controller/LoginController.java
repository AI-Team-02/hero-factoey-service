package ai.herofactoryservice.login.controller;


import ai.herofactoryservice.login.dto.LoginResponseDto;
import ai.herofactoryservice.login.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/auth")
public class LoginController {
    private final TokenService tokenService;
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        String accessToken = bearerToken.substring(7);
        tokenService.logout(accessToken);
        return ResponseEntity.ok().build();
    }

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
