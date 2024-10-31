package ai.herofactoryservice.create_game_resource_service.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "prompts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "prompt_id")
    private String promptId;

    @Column(nullable = false)
    private String memberId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalPrompt;

    @Column(columnDefinition = "TEXT")
    private String improvedPrompt;

    @Column(name = "embedding_vector", columnDefinition = "vector")
    private double[] embeddingVector;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, List<String>>> categoryKeywords = new ArrayList<>();

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PromptStatus status = PromptStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}