package ai.herofactoryservice.create_game_resource_service.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    @Transactional
    public void initializeDatabase() {
        try {
            // pgvector 확장 설치
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");

            // 필요한 경우 여기에 추가 초기화 로직

        } catch (Exception e) {
            // 이미 설치되어 있거나 권한 문제 등의 예외 처리
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
}