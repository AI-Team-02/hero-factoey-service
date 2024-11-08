package ai.herofactoryservice.prompt.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.hypersistence.utils.hibernate.type.array.DoubleArrayType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ai.herofactoryservice.prompt.entity.enums.PromptStatus;

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
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "prompt_id", unique = true, nullable = false)
    private String promptId;

    @Column(nullable = false)
    private String memberId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalPrompt;

    @Column(columnDefinition = "TEXT")
    private String improvedPrompt;

    @Type(DoubleArrayType.class)
    @Column(name = "embedding_vector", columnDefinition = "vector(1536)")
    private double[] embeddingVector;

    @Type(JsonType.class)
    @Column(name = "category_keywords", columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, List<String>>> categoryKeywords = new ArrayList<>();

    @Type(JsonType.class)
    @Column(name = "keywords", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
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

    @OneToMany(mappedBy = "prompt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PromptLog> promptLogs = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 편의 메서드 추가
    public void addPromptLog(PromptLog log) {
        this.promptLogs.add(log);
        log.setPrompt(this);
    }
}