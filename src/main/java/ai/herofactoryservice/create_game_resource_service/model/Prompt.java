package ai.herofactoryservice.create_game_resource_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "prompts",
        indexes = {
                @Index(name = "idx_prompt_id", columnList = "promptId"),
                @Index(name = "idx_member_id", columnList = "memberId"),
                @Index(name = "idx_status", columnList = "status")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String promptId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalPrompt;

    @Column(columnDefinition = "TEXT")
    private String enhancedPrompt;

    @Column(columnDefinition = "vector(1536)")
    private double[] embedding;

    @Convert(converter = ai.herofactoryservice.create_game_resource_service.converter.CategoryKeywordsConverter.class)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, List<String>>> categoryKeywords;  // 카테고리별 키워드 저장

    @ElementCollection
    @CollectionTable(name = "prompt_keywords",
            joinColumns = @JoinColumn(name = "prompt_id"))
    private List<String> keywords;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromptStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    @Column(length = 500)
    private String errorMessage;
}