package com.herofactory.config.prompt;

import com.herofactory.prompt.repository.CustomVectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Profile("test")
@Slf4j
public class DatabaseConfig {
    private final JdbcTemplate jdbcTemplate;
    private final CustomVectorRepository customVectorRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDatabase() {
        try {
            log.info("Starting database initialization...");

            // 1. DB 연결 확인
            checkDatabaseConnection();

            // 2. 기존 테이블 삭제 (역순)
            dropTablesInOrder();

            // 3. vector 확장 초기화
            initializeVectorExtension();

            // 4. 테이블 생성 (직접 SQL 실행)
            createTables();

            // 5. 인덱스 생성
            createAllIndexes();

            log.info("Database initialization completed successfully");
        } catch (Exception e) {
            log.error("Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void checkDatabaseConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("Database connection successful");
        } catch (Exception e) {
            log.error("Database connection failed", e);
            throw new RuntimeException("Unable to connect to database", e);
        }
    }

    private void dropTablesInOrder() {
        String[] tables = {
                "subscription_payments",
                "prompt_logs",
                "payment_logs",
                "message_logs",
                "payments",
                "prompts"
        };

        for (String table : tables) {
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + table + " CASCADE");
            log.info("Dropped table if exists: {}", table);
        }
    }

    private void initializeVectorExtension() {
        log.info("Initializing vector extension...");
        try {
            Boolean extensionExists = jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'vector')",
                    Boolean.class
            );

            if (Boolean.FALSE.equals(extensionExists)) {
                jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
                log.info("Vector extension created successfully");
                Thread.sleep(1000);
            } else {
                log.info("Vector extension already exists");
            }
        } catch (Exception e) {
            log.error("Failed to initialize vector extension", e);
            throw new RuntimeException("Vector extension initialization failed", e);
        }
    }

    private void createTables() {
        log.info("Creating tables...");

        // prompts 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE prompts (
                id UUID PRIMARY KEY,
                prompt_id VARCHAR(255) UNIQUE NOT NULL,
                member_id VARCHAR(255) NOT NULL,
                original_prompt TEXT NOT NULL,
                improved_prompt TEXT,
                keywords JSONB DEFAULT '[]'::jsonb,
                category_keywords JSONB DEFAULT '[]'::jsonb,
                embedding_vector vector(1536),
                status VARCHAR(50) NOT NULL,
                error_message TEXT,
                created_at TIMESTAMP NOT NULL,
                completed_at TIMESTAMP,
                updated_at TIMESTAMP NOT NULL
            )
        """);
        log.info("Created prompts table");

        // payments 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE payments (
                id BIGSERIAL PRIMARY KEY,
                payment_id VARCHAR(36) UNIQUE NOT NULL,
                order_id VARCHAR(255) NOT NULL,
                tid VARCHAR(255) NOT NULL,
                shop_item_id BIGINT NOT NULL,
                member_id VARCHAR(255) NOT NULL,
                amount BIGINT NOT NULL,
                item_name VARCHAR(255) NOT NULL,
                status VARCHAR(20) NOT NULL,
                created_at TIMESTAMP NOT NULL,
                updated_at TIMESTAMP,
                approved_at TIMESTAMP,
                canceled_at TIMESTAMP,
                cancel_amount BIGINT,
                cancel_reason VARCHAR(255),
                payment_key VARCHAR(100),
                error_message VARCHAR(500)
            )
        """);
        log.info("Created payments table");

        // payment_logs 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE payment_logs (
                id BIGSERIAL PRIMARY KEY,
                payment_id VARCHAR(36) NOT NULL,
                log_type VARCHAR(50) NOT NULL,
                content TEXT NOT NULL,
                created_at TIMESTAMP NOT NULL,
                FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE CASCADE
            )
        """);
        log.info("Created payment_logs table");

        // prompt_logs 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE prompt_logs (
                id BIGSERIAL PRIMARY KEY,
                prompt_id VARCHAR(255),
                log_type VARCHAR(50) NOT NULL,
                content TEXT,
                created_at TIMESTAMP NOT NULL,
                FOREIGN KEY (prompt_id) REFERENCES prompts(prompt_id) ON DELETE CASCADE
            )
        """);
        log.info("Created prompt_logs table");

        // message_logs 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE message_logs (
                id BIGSERIAL PRIMARY KEY,
                message_id VARCHAR(255) NOT NULL,
                prompt_id VARCHAR(255),
                payment_id VARCHAR(255),
                status VARCHAR(50) NOT NULL,
                error_message TEXT,
                retry_count INTEGER DEFAULT 0,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // subscription_payments 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE subscription_payments (
                id BIGSERIAL PRIMARY KEY,
                external_payment_id VARCHAR(255) NOT NULL,
                subscription_id BIGINT NOT NULL,
                payment_id VARCHAR(36),
                amount BIGINT NOT NULL,
                status VARCHAR(50) NOT NULL,
                billing_date TIMESTAMP NOT NULL,
                paid_at TIMESTAMP,
                failure_reason VARCHAR(500),
                retry_count INTEGER DEFAULT 0,
                created_at TIMESTAMP NOT NULL,
                updated_at TIMESTAMP
             )
        """);

        log.info("Created message_logs table");
    }

    private void createAllIndexes() {
        log.info("Creating indexes...");

        List<IndexDefinition> indexes = List.of(
                // 기본 인덱스들
                new IndexDefinition("idx_prompts_prompt_id", "prompts", "prompt_id"),
                new IndexDefinition("idx_prompt_logs_prompt_id", "prompt_logs", "prompt_id"),
                new IndexDefinition("idx_message_logs_message_id", "message_logs", "message_id"),
                new IndexDefinition("idx_message_status", "message_logs", "message_id, status"),
                new IndexDefinition("idx_payments_payment_id", "payments", "payment_id"),
                new IndexDefinition("idx_payments_order_id", "payments", "order_id"),
                new IndexDefinition("idx_payments_member_id", "payments", "member_id"),
                new IndexDefinition("idx_payments_status", "payments", "status"),
                new IndexDefinition("idx_payment_logs_payment_id_created_at", "payment_logs", "payment_id, created_at"),

                // 추가 인덱스들
                new IndexDefinition("idx_message_payment", "message_logs", "payment_id"),
                new IndexDefinition("idx_message_prompt", "message_logs", "prompt_id"),
                new IndexDefinition("idx_message_composite", "message_logs", "message_id, status, prompt_id")
        );

        for (IndexDefinition index : indexes) {
            try {
                if (!indexExists(index.name())) {
                    jdbcTemplate.execute(String.format(
                            "CREATE INDEX IF NOT EXISTS %s ON %s (%s)",
                            index.name(), index.table(), index.columns()
                    ));
                    log.info("Created index: {}", index.name());
                }
            } catch (Exception e) {
                log.warn("Error creating index {}: {}", index.name(), e.getMessage());
            }
        }

        // Vector 인덱스 생성
        try {
            if (!indexExists("prompt_vector_idx")) {
                jdbcTemplate.execute("""
                    CREATE INDEX IF NOT EXISTS prompt_vector_idx 
                    ON prompts USING ivfflat (embedding_vector vector_l2_ops)
                    WITH (lists = 100)
                """);
                log.info("Created vector similarity index");
            }
        } catch (Exception e) {
            log.warn("Failed to create vector index - will be created when data is available: {}", e.getMessage());
        }
    }

    private boolean indexExists(String indexName) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = ?)",
                Boolean.class,
                indexName.toLowerCase()
        ));
    }

    private record IndexDefinition(String name, String table, String columns) {}
}