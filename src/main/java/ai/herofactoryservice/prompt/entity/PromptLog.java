package ai.herofactoryservice.prompt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_logs",
        indexes = {
                @Index(name = "idx_prompt_id_created_at", columnList = "prompt_id,created_at")
        })
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    @Column(nullable = false)
    private String logType;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}