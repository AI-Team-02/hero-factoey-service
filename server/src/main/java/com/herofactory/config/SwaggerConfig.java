package com.herofactory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hero Factory API")
                        .description("""
                                Hero Factory Service API Documentation
                                                            
                                ## 로그인이 필요한 API 테스트 방법
                                                            
                                1. http://localhost:8080/test 페이지에 접속
                                2. 카카오 로그인 완료
                                3. 발급받은 액세스 토큰을 복사
                                4. Swagger 우측 상단의 'Authorize' 버튼 클릭
                                5. 복사한 액세스 토큰 입력
                                6. Authorize 버튼 클릭하여 저장
                                7. 이제 로그인이 필요한 API를 테스트할 수 있습니다.
                                """)
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력해주세요.")
                        ));
    }
}