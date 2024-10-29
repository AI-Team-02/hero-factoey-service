package ai.herofactoryservice.create_game_resource_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_results", indexes = {
        @Index(name = "idx_prompt_id", columnList = "promptId"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter @Setter
public class PromptResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String promptId;

    @Column(columnDefinition = "TEXT")
    private String originalPrompt;

    @Column(columnDefinition = "TEXT")
    private String enhancedPrompt;

    @Column(columnDefinition = "TEXT")
    private String result;

    private Integer tokenCount;
    private Long processingTimeMs;

    @Column(columnDefinition = "TEXT")
    private String errorDetails;

    @Enumerated(EnumType.STRING)
    private PromptStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PromptStatus.PENDING;
        }
    }
    public enum PromptStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}