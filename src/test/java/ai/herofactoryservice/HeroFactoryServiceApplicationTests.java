package ai.herofactoryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // test 프로파일 사용
class HeroFactoryServiceApplicationTests {

    @Test
    void contextLoads() {
        // 빈 테스트 - 컨텍스트 로딩만 확인
    }
}