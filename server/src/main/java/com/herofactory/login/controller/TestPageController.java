package com.herofactory.login.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestPageController {
    /**
     * TODO : 프론트 연결 시, 삭제 예정
     * 카카오 간편 로그인 테스트용
     */
    @GetMapping("/test")
    public String testPage() {
        return "test";
    }
}