//package ai.herofactoryservice.create_game_resource_service.security;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ai.herofactoryservice.create_game_resource_service.model.KakaoUserInfo;
//import ai.herofactoryservice.create_game_resource_service.model.Member;
//import ai.herofactoryservice.create_game_resource_service.model.Provider;
//import ai.herofactoryservice.create_game_resource_service.repository.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
//
//    private final MemberRepository memberRepository;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    @Transactional
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        try {
//            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
//            OAuth2User oAuth2User = delegate.loadUser(userRequest);
//
//            log.debug("OAuth2 Provider: {}", userRequest.getClientRegistration().getRegistrationId());
//            log.debug("OAuth2User attributes: {}", oAuth2User.getAttributes());
//
//            if ("kakao".equals(userRequest.getClientRegistration().getRegistrationId())) {
//                return processKakaoUser(oAuth2User);
//            }
//
//            throw new OAuth2AuthenticationException("Unsupported provider");
//
//        } catch (Exception e) {
//            log.error("OAuth2 authentication failed", e);
//            throw new OAuth2AuthenticationException("Authentication failed", e);
//        }
//    }
//
//    private OAuth2User processKakaoUser(OAuth2User oAuth2User) {
//        try {
//            Map<String, Object> attributes = oAuth2User.getAttributes();
//            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
//            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
//
//            String email = (String) kakaoAccount.get("email");
//            String nickname = (String) profile.get("nickname");
//            String providerId = String.valueOf(attributes.get("id"));
//
//            log.debug("Kakao user info - email: {}, nickname: {}, providerId: {}",
//                    email, nickname, providerId);
//
//            Member member = memberRepository.findByProviderAndProviderId(Provider.KAKAO, providerId)
//                    .orElseGet(() -> {
//                        log.debug("Creating new Kakao user");
//                        return createKakaoUser(email, nickname, providerId);
//                    });
//
//            return new CustomUserDetails(member);
//        } catch (Exception e) {
//            log.error("Error processing Kakao user", e);
//            throw new OAuth2AuthenticationException("Failed to process Kakao user", e);
//        }
//    }
//
//    private Member createKakaoUser(String email, String nickname, String providerId) {
//        if (email == null || nickname == null || providerId == null) {
//            throw new OAuth2AuthenticationException("Required user info is missing");
//        }
//
//        Member member = Member.builder()
//                .email(email)
//                .nickname(nickname)
//                .provider(Provider.KAKAO)
//                .providerId(providerId)
//                .build();
//
//        return memberRepository.save(member);
//    }
//}