//package ai.herofactoryservice.create_game_resource_service.service;
//
//import ai.herofactoryservice.create_game_resource_service.exception.AuthenticationException;
//import ai.herofactoryservice.create_game_resource_service.model.Member;
//import ai.herofactoryservice.create_game_resource_service.repository.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import jakarta.servlet.http.HttpServletRequest;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//    private final MemberRepository memberRepository;
//
//    public Member getCurrentUser(Authentication authentication) {
//        if (authentication == null) {
//            throw new AuthenticationException("Not authenticated");
//        }
//
//        return memberRepository.findByEmail(authentication.getName())
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//    }
//
//    public void logout(HttpServletRequest request) {
//        // JWT는 서버에서 관리하지 않으므로, 클라이언트에서 토큰을 삭제하도록 안내
//    }
//}