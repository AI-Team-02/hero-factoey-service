package ai.herofactoryservice.login.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestPageController {
    
    @GetMapping("/test")
    public String testPage() {
        return "test";
    }
}