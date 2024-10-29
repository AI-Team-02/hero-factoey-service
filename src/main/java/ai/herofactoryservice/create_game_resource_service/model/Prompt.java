package ai.herofactoryservice.create_game_resource_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

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
    private String promptId;    // UUID로 생성되는 고유 ID

    @Column(nullable = false)
    private Long memberId;      // 회원 ID

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalPrompt;  // 원본 프롬프트

    @Column(columnDefinition = "TEXT")
    private String enhancedPrompt;  // 개선된 프롬프트

    @Column(columnDefinition = "vector(1536)")  // OpenAI 임베딩 차원
    private Float[] embedding;      // 벡터 임베딩

    @ElementCollection
    @CollectionTable(name = "prompt_keywords",
            joinColumns = @JoinColumn(name = "prompt_id"))
    private List<String> keywords;  // 추천 키워드 리스트

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromptStatus status;  // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    @Column(length = 500)
    private String errorMessage;
}