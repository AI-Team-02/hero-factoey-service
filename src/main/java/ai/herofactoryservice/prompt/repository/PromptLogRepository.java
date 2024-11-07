package ai.herofactoryservice.prompt.repository;

import ai.herofactoryservice.prompt.entity.Prompt;
import ai.herofactoryservice.prompt.entity.PromptLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromptLogRepository extends JpaRepository<PromptLog, Long> {
    List<PromptLog> findByPromptOrderByCreatedAtDesc(Prompt prompt);

    @Query("SELECT pl FROM PromptLog pl WHERE pl.logType = :logType AND pl.createdAt > :since")
    List<PromptLog> findRecentLogsByType(
            @Param("logType") String logType,
            @Param("since") LocalDateTime since
    );

    @Query("SELECT pl FROM PromptLog pl WHERE pl.prompt.promptId = :promptId AND pl.logType = :logType " +
            "ORDER BY pl.createdAt DESC")
    List<PromptLog> findByPromptIdAndLogType(
            @Param("promptId") String promptId,
            @Param("logType") String logType
    );

    @Query("SELECT COUNT(pl) FROM PromptLog pl WHERE pl.prompt.promptId = :promptId AND pl.logType = :logType")
    long countByPromptIdAndLogType(
            @Param("promptId") String promptId,
            @Param("logType") String logType
    );
}