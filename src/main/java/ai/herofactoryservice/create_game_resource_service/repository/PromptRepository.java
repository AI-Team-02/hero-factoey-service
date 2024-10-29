package ai.herofactoryservice.create_game_resource_service.repository;

import ai.herofactoryservice.create_game_resource_service.model.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    Optional<Prompt> findByPromptId(String promptId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Prompt p WHERE p.promptId = :promptId")
    Optional<Prompt> findByPromptIdWithLock(@Param("promptId") String promptId);

    @Query(value = """
        SELECT p.*, 
        (p.embedding <-> :queryEmbedding) as distance
        FROM prompts p
        WHERE p.status = 'COMPLETED'
        ORDER BY distance
        LIMIT :limit
        """, nativeQuery = true)
    List<Prompt> findSimilarPrompts(
            @Param("queryEmbedding") Float[] queryEmbedding,
            @Param("limit") int limit
    );
}