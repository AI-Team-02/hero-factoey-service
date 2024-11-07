package ai.herofactoryservice.prompt.repository;

import ai.herofactoryservice.prompt.entity.Prompt;
import ai.herofactoryservice.prompt.entity.enums.PromptStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomVectorRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    // JSON 파싱 메서드들
    private List<String> parseJsonToStringList(String json) {
        if (json == null) return new ArrayList<>();
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            log.warn("Error parsing json to string list: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, List<String>>> parseJsonToCategoryKeywords(String json) {
        if (json == null) return new ArrayList<>();
        try {
            var typeFactory = objectMapper.getTypeFactory();

            // Step 1: Create List<String> type
            var stringListType = typeFactory.constructCollectionType(List.class, String.class);

            // Step 2: Create Map<String, List<String>> type
            var stringType = typeFactory.constructType(String.class);
            var mapType = typeFactory.constructMapLikeType(Map.class, stringType, stringListType);

            // Step 3: Create final List<Map<String, List<String>>> type
            var finalType = typeFactory.constructCollectionType(List.class, mapType);

            return objectMapper.readValue(json, finalType);
        } catch (Exception e) {
            log.warn("Error parsing json to category keywords: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private final RowMapper<Prompt> promptRowMapper = (rs, rowNum) -> {
        Prompt prompt = new Prompt();
        prompt.setPromptId(rs.getString("prompt_id"));
        prompt.setMemberId(rs.getString("member_id"));
        prompt.setOriginalPrompt(rs.getString("original_prompt"));
        prompt.setImprovedPrompt(rs.getString("improved_prompt"));

        // JSON 파싱을 별도 메서드로 분리
        prompt.setKeywords(parseJsonToStringList(rs.getString("keywords")));
        prompt.setCategoryKeywords(parseJsonToCategoryKeywords(rs.getString("category_keywords")));

        // Vector 처리
        try {
            Object vectorObj = rs.getObject("embedding_vector");
            if (vectorObj != null) {
                String vectorStr = vectorObj.toString().replaceAll("[\\[\\]{}]", "");
                String[] values = vectorStr.split(",");
                double[] vector = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    vector[i] = Double.parseDouble(values[i].trim());
                }
                prompt.setEmbeddingVector(vector);
            }
        } catch (Exception e) {
            log.warn("Error parsing embedding vector: {}", e.getMessage());
        }

        prompt.setStatus(PromptStatus.valueOf(rs.getString("status")));
        prompt.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        prompt.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        java.sql.Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            prompt.setCompletedAt(completedAt.toLocalDateTime());
        }

        prompt.setErrorMessage(rs.getString("error_message"));

        return prompt;
    };

    public void initializeDatabase() {
        try {
            // Enable vector extension
            enableVectorExtension();

            // Create tables if they don't exist
            createTablesIfNotExist();

            // Create indexes if they don't exist
            createIndexesIfNotExist();

        } catch (Exception e) {
            log.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void enableVectorExtension() {
        log.info("Checking vector extension...");
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_extension WHERE extname = 'vector'",
                Integer.class
        );

        if (count == null || count == 0) {
            log.info("Creating vector extension...");
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
            // 확장 생성 후 잠시 대기하여 확장이 완전히 로드되도록 함
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("Vector extension is ready");
    }

    private void createTablesIfNotExist() {
        log.info("Creating tables if they don't exist...");

        // prompts 테이블이 존재하면 삭제
        if (tableExists("prompts")) {
            jdbcTemplate.execute("DROP TABLE prompts CASCADE");
        }

        // 테이블 생성
        jdbcTemplate.execute("""
            CREATE TABLE prompts (
                id UUID PRIMARY KEY,
                prompt_id VARCHAR(255),
                member_id VARCHAR(255) NOT NULL,
                original_prompt TEXT NOT NULL,
                improved_prompt TEXT,
                keywords JSONB DEFAULT '[]'::jsonb,
                category_keywords JSONB DEFAULT '[]'::jsonb,
                status VARCHAR(50) NOT NULL,
                error_message TEXT,
                created_at TIMESTAMP NOT NULL,
                completed_at TIMESTAMP,
                updated_at TIMESTAMP NOT NULL,
                CONSTRAINT uk_prompt_id UNIQUE (prompt_id)
            )
        """);

        // vector 컬럼 별도로 추가
        jdbcTemplate.execute("""
            ALTER TABLE prompts 
            ADD COLUMN embedding_vector vector(1536)
        """);

        log.info("Created prompts table with vector column");

        if (!tableExists("message_logs")) {
            jdbcTemplate.execute("""
                CREATE TABLE message_logs (
                    id BIGSERIAL PRIMARY KEY,
                    message_id VARCHAR(255) NOT NULL,
                    prompt_id VARCHAR(255),
                    payment_id VARCHAR(255),
                    status VARCHAR(50) NOT NULL,
                    error_message TEXT,
                    retry_count INTEGER DEFAULT 0,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
            """);
            log.info("Created message_logs table");
        }
    }

    private void createIndexesIfNotExist() {
        log.info("Creating indexes if they don't exist...");

        // 기본 인덱스들 먼저 생성
        if (!indexExists("idx_prompts_prompt_id")) {
            jdbcTemplate.execute("CREATE INDEX idx_prompts_prompt_id ON prompts(prompt_id)");
        }
        if (!indexExists("idx_message_logs_message_id")) {
            jdbcTemplate.execute("CREATE INDEX idx_message_logs_message_id ON message_logs(message_id)");
        }
        if (!indexExists("idx_message_status")) {
            jdbcTemplate.execute("CREATE INDEX idx_message_status ON message_logs(message_id, status)");
        }

        // vector 컬럼이 존재하는지 확인 후 vector 인덱스 생성
        Boolean hasVectorColumn = jdbcTemplate.queryForObject("""
            SELECT EXISTS (
                SELECT 1 
                FROM information_schema.columns 
                WHERE table_name = 'prompts' 
                AND column_name = 'embedding_vector'
            )
            """, Boolean.class);

        if (Boolean.TRUE.equals(hasVectorColumn) && !indexExists("prompt_vector_idx")) {
            try {
                jdbcTemplate.execute("""
                    CREATE INDEX prompt_vector_idx 
                    ON prompts USING ivfflat (embedding_vector vector_l2_ops) 
                    WITH (lists = 100)
                """);
                log.info("Created vector similarity index");
            } catch (Exception e) {
                log.warn("Failed to create vector index - will be created when data is available", e);
            }
        }
    }

    private boolean tableExists(String tableName) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?)",
                Boolean.class,
                tableName
        ));
    }

    private boolean indexExists(String indexName) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = ?)",
                Boolean.class,
                indexName
        ));
    }

    public void savePromptWithVector(Prompt prompt) {
        String vectorStr = vectorToString(prompt.getEmbeddingVector());

        if (prompt.getId() == null) {
            prompt.setId(UUID.randomUUID());
        }

        // vector 타입 캐스팅을 SQL 문자열에 직접 포함
        String sql = String.format("""
            INSERT INTO prompts (
                id, prompt_id, member_id, original_prompt, improved_prompt, 
                embedding_vector, status, created_at, updated_at,
                completed_at, error_message, keywords, category_keywords
            ) VALUES (?, ?, ?, ?, ?, 
                     %s,
                     ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb)
            ON CONFLICT (prompt_id) DO UPDATE SET
                improved_prompt = EXCLUDED.improved_prompt,
                embedding_vector = EXCLUDED.embedding_vector,
                status = EXCLUDED.status,
                updated_at = EXCLUDED.updated_at,
                completed_at = EXCLUDED.completed_at,
                error_message = EXCLUDED.error_message,
                keywords = EXCLUDED.keywords,
                category_keywords = EXCLUDED.category_keywords
            """,
                vectorStr == null ? "NULL" : "'" + vectorStr + "'::vector(1536)");

        try {
            jdbcTemplate.update(sql,
                    prompt.getId(),
                    prompt.getPromptId(),
                    prompt.getMemberId(),
                    prompt.getOriginalPrompt(),
                    prompt.getImprovedPrompt(),
                    prompt.getStatus().name(),
                    prompt.getCreatedAt(),
                    prompt.getUpdatedAt(),
                    prompt.getCompletedAt(),
                    prompt.getErrorMessage(),
                    convertToJsonString(prompt.getKeywords()),
                    convertToJsonString(prompt.getCategoryKeywords())
            );
        } catch (Exception e) {
            log.error("Error saving prompt with vector", e);
            throw new RuntimeException("Failed to save prompt: " + e.getMessage(), e);
        }
    }

    private String vectorToString(double[] vector) {
        if (vector == null) return null;
        return "[" + String.join(",",
                Arrays.stream(vector)
                        .mapToObj(String::valueOf)
                        .toArray(String[]::new)) + "]";
    }

    public List<Prompt> findSimilarPrompts(double[] vector, double threshold, int limit) {
        String sql = """
            SELECT * FROM prompts
            WHERE embedding_vector IS NOT NULL 
            AND 1 - (embedding_vector <=> cast(? as vector(1536))) > ?
            ORDER BY 1 - (embedding_vector <=> cast(? as vector(1536))) DESC
            LIMIT ?
            """;

        String vectorStr = vectorToString(vector);
        return jdbcTemplate.query(sql, promptRowMapper,
                vectorStr, threshold, vectorStr, limit);
    }

    private String convertToJsonString(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON string", e);
            throw new RuntimeException("Failed to convert object to JSON: " + e.getMessage(), e);
        }
    }

    public Optional<Prompt> findByPromptId(String promptId) {
        String sql = "SELECT * FROM prompts WHERE prompt_id = ?";
        List<Prompt> prompts = jdbcTemplate.query(sql, promptRowMapper, promptId);
        return prompts.isEmpty() ? Optional.empty() : Optional.of(prompts.get(0));
    }
}