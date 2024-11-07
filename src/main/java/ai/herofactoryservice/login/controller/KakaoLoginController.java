package ai.herofactoryservice.login.controller;

import ai.herofactoryservice.login.dto.LoginResponseDto;
import ai.herofactoryservice.login.service.KakaoLoginService;
import ai.herofactoryservice.login.service.TokenService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Kakao Login", description = "카카오 OAuth 로그인 관련 API")
public class KakaoLoginController {
    private final KakaoLoginService kakaoLoginService;

    @Operation(
            summary = "카카오 로그인 시작",
            description = "카카오 OAuth 인증 페이지로 리다이렉트됩니다. \n\n"
            +"redirect가 되기 때문에, Swagger Test시 오류가 나는 것은 정상입니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "카카오 로그인 페이지로 리다이렉트됩니다."
            )
    })
    @GetMapping("/login")
    public String kakaoLogin() {
        log.info("Kakao login initiated");
        String kakaoAuthUrl = kakaoLoginService.getKakaoLoginUrl();
        return "redirect:" + kakaoAuthUrl;
    }

    @Hidden
    @GetMapping("/callback")
    @ResponseBody
    public LoginResponseDto kakaoCallback(@RequestParam String code) {
        log.info("Kakao callback received with code");
        log.debug("code = {}", code);
        return kakaoLoginService.processKakaoLogin(code);
    }
}
