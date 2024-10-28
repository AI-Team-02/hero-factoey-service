package ai.herofactoryservice.login.service;

import ai.herofactoryservice.config.security.JwtTokenProvider;
import ai.herofactoryservice.login.dto.KakaoTokenResponseDto;
import ai.herofactoryservice.login.dto.KakaoUserInfoResponseDto;
import ai.herofactoryservice.login.dto.LoginResponseDto;
import ai.herofactoryservice.login.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoLoginService {

    private final RestTemplate restTemplate;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${kakao.client.id}")
    private String clientId;
    
    private static final String AUTH_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String REDIRECT_URI = "http://localhost:8080/api/kakao/callback";
    
    public String getKakaoLoginUrl() {
        return AUTH_URL + "?client_id=" + clientId 
                      + "&redirect_uri=" + REDIRECT_URI 
                      + "&response_type=code";
    }
    
    public LoginResponseDto processKakaoLogin(String code) {
        String accessToken = getAccessToken(code);
        KakaoUserInfoResponseDto userInfo = getUserInfo(accessToken);

        // 회원가입 처리
        User user = userService.registerKakaoUser(userInfo);
        String jwtToken = jwtTokenProvider.createToken(user.getId());

        return LoginResponseDto.builder()
                .accessToken(jwtToken)
                .build();
    }
    
    private String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", REDIRECT_URI);
        body.add("code", code);
        
        HttpEntity<MultiValueMap<String, String>> request =
            new HttpEntity<>(body, headers);

        try {
            ResponseEntity<KakaoTokenResponseDto> response = restTemplate.postForEntity(
                TOKEN_URL,
                request,
                KakaoTokenResponseDto.class
            );
            
            if (response.getBody() != null) {
                return response.getBody().getAccess_token();
            }
            throw new RuntimeException("Failed to get access token");
        } catch (Exception e) {
            log.error("Failed to get access token", e);
            throw new RuntimeException("Failed to get access token", e);
        }
    }

    private KakaoUserInfoResponseDto getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfoResponseDto> response = restTemplate.exchange(
                    USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    KakaoUserInfoResponseDto.class
            );

            if (response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("Failed to get user info");
        } catch (Exception e) {
            log.error("Failed to get user info", e);
            throw new RuntimeException("Failed to get user info", e);
        }
    }
}