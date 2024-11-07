package ai.herofactoryservice.config.prompt;

import ai.herofactoryservice.prompt.repository.CustomVectorRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseConfig {

    private final JdbcTemplate jdbcTemplate;
    private final CustomVectorRepository customVectorRepository;

    @PostConstruct
    @Transactional
    public void initializeDatabase() {
        try {
            log.info("Starting database initialization...");
            initializeVectorExtension();
            initializeRepositories();
            log.info("Database initialization completed successfully");
        } catch (Exception e) {
            log.error("Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void initializeVectorExtension() {
        try {
            log.info("Initializing pgvector extension...");
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            log.info("pgvector extension initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize pgvector extension", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void initializeRepositories() {
        try {
            log.info("Initializing vector repository...");
            customVectorRepository.initializeDatabase();
            log.info("Vector repository initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize vector repository", e);
            throw new RuntimeException("Vector repository initialization failed", e);
        }
    }
}