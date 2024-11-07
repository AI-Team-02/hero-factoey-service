package ai.herofactoryservice.prompt.repository;

import ai.herofactoryservice.create_game_resource_service.model.PromptResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromptResultRepository extends JpaRepository<PromptResult, Long> {
    Optional<PromptResult> findByPromptId(String promptId);

    List<PromptResult> findByStatus(PromptResult.PromptStatus status);

    @Query("SELECT pr FROM PromptResult pr WHERE pr.status = :status AND pr.createdAt < :cutoffTime")
    List<PromptResult> findStaleResults(
            @Param("status") PromptResult.PromptStatus status,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    @Query("SELECT AVG(pr.processingTimeMs) FROM PromptResult pr WHERE pr.status = 'COMPLETED'")
    Double getAverageProcessingTime();
}