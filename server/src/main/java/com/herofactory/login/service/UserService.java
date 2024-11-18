package com.herofactory.login.service;

import com.herofactory.login.dto.KakaoUserInfoResponseDto;
import com.herofactory.login.entity.User;
import com.herofactory.login.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
   
   private final UserRepository userRepository;

   public User registerKakaoUser(KakaoUserInfoResponseDto kakaoUserInfo) {
       User user = userRepository.findByKakaoId(kakaoUserInfo.getId());
       if (user != null) {
           log.info("Existing user found: {}", user.getEmail());
           return user;
       }

       log.info("New user registration with email: {}", kakaoUserInfo.getKakao_account().getEmail());
       return userRepository.save(
           User.builder()
               .kakaoId(kakaoUserInfo.getId())
               .email(kakaoUserInfo.getKakao_account().getEmail())
               .name(kakaoUserInfo.getKakao_account().getName())
               .phoneNumber(kakaoUserInfo.getKakao_account().getPhone_number())
               .build()
       );
   }
}