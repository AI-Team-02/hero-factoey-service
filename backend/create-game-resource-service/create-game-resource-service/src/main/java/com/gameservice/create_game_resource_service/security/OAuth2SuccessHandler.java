package com.gameservice.create_game_resource_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameservice.create_game_resource_service.model.KakaoUserInfo;
import com.gameservice.create_game_resource_service.model.Member;
import com.gameservice.create_game_resource_service.model.Provider;
import com.gameservice.create_game_resource_service.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        KakaoUserInfo kakaoUser = objectMapper.convertValue(oAuth2User.getAttributes(), KakaoUserInfo.class);

        String email = kakaoUser.getKakaoAccount().getEmail();
        String nickname = kakaoUser.getKakaoAccount().getProfile().getNickname();
        String providerId = String.valueOf(kakaoUser.getId());

        Member member = memberRepository.findByProviderAndProviderId(Provider.KAKAO, providerId)
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .email(email)
                        .nickname(nickname)
                        .provider(Provider.KAKAO)
                        .providerId(providerId)
                        .build()));

        String token = jwtTokenProvider.createToken(member);

        String targetUrl = UriComponentsBuilder.fromUriString("/login/success")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}