package ai.herofactoryservice.create_game_resource_service.config;

import ai.herofactoryservice.create_game_resource_service.repository.CustomVectorRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class VectorConfig {
    private final CustomVectorRepository customVectorRepository;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing database...");
            customVectorRepository.initializeDatabase();
            log.info("Database initialization completed");
        } catch (Exception e) {
            log.error("Failed to initialize database", e);
            throw e;
        }
    }
}