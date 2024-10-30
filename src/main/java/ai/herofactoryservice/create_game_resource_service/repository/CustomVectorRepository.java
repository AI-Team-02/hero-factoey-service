package ai.herofactoryservice.create_game_resource_service.repository;

import ai.herofactoryservice.create_game_resource_service.model.Prompt;
import ai.herofactoryservice.create_game_resource_service.model.PromptStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomVectorRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private final RowMapper<Prompt> promptRowMapper = (rs, rowNum) -> {
        Prompt prompt = new Prompt();
        prompt.setPromptId(rs.getString("prompt_id"));
        prompt.setMemberId(rs.getString("member_id"));
        prompt.setOriginalPrompt(rs.getString("original_prompt"));
        prompt.setImprovedPrompt(rs.getString("improved_prompt"));

        Array vectorArray = rs.getArray("embedding_vector");
        if (vectorArray != null) {
            Number[] numbers = (Number[]) vectorArray.getArray();
            double[] vector = new double[numbers.length];
            for (int i = 0; i < numbers.length; i++) {
                vector[i] = numbers[i].doubleValue();
            }
            prompt.setEmbeddingVector(vector);
        }

        prompt.setStatus(PromptStatus.valueOf(rs.getString("status")));
        prompt.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        prompt.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        prompt.setCompletedAt(rs.getTimestamp("completed_at") != null ?
                rs.getTimestamp("completed_at").toLocalDateTime() : null);
        prompt.setErrorMessage(rs.getString("error_message"));

        return prompt;
    };

    public void savePromptWithVector(Prompt prompt) {
        String sql = """
            INSERT INTO prompts (
                prompt_id, member_id, original_prompt, improved_prompt, 
                embedding_vector, status, created_at, updated_at,
                completed_at, error_message, keywords, category_keywords
            ) VALUES (?, ?, ?, ?, ?::vector, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb)
            ON CONFLICT (prompt_id) DO UPDATE SET
                improved_prompt = EXCLUDED.improved_prompt,
                embedding_vector = EXCLUDED.embedding_vector,
                status = EXCLUDED.status,
                updated_at = EXCLUDED.updated_at,
                completed_at = EXCLUDED.completed_at,
                error_message = EXCLUDED.error_message,
                keywords = EXCLUDED.keywords,
                category_keywords = EXCLUDED.category_keywords
            """;

        try {
            jdbcTemplate.update(sql,
                    prompt.getPromptId(),
                    prompt.getMemberId(),
                    prompt.getOriginalPrompt(),
                    prompt.getImprovedPrompt(),
                    vectorToString(prompt.getEmbeddingVector()),
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

    private String convertToJsonString(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON string", e);
            throw new RuntimeException("Failed to convert object to JSON: " + e.getMessage(), e);
        }
    }

    public List<Prompt> findSimilarPrompts(double[] vector, double threshold, int limit) {
        String sql = """
            SELECT * FROM prompts
            WHERE 1 - (embedding_vector <=> ?::vector) > ?
            ORDER BY 1 - (embedding_vector <=> ?::vector) DESC
            LIMIT ?
            """;

        String vectorStr = vectorToString(vector);
        return jdbcTemplate.query(sql, promptRowMapper,
                vectorStr, threshold, vectorStr, limit);
    }

    private String vectorToString(double[] vector) {
        if (vector == null) return null;
        return "[" + String.join(",",
                java.util.Arrays.stream(vector)
                        .mapToObj(String::valueOf)
                        .toArray(String[]::new)) + "]";
    }

    public void createVectorExtensionAndIndex() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("""
            DO $$ 
            BEGIN
                IF NOT EXISTS (
                    SELECT 1 FROM pg_class c 
                    JOIN pg_namespace n ON n.oid = c.relnamespace 
                    WHERE c.relname = 'prompt_vector_idx'
                ) THEN
                    CREATE INDEX prompt_vector_idx 
                    ON prompts USING ivfflat (embedding_vector vector_l2_ops)
                    WITH (lists = 100);
                END IF;
            END $$;
            """);
    }
}