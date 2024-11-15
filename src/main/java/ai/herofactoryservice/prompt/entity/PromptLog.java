package ai.herofactoryservice.prompt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_logs")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", referencedColumnName = "prompt_id")
    private Prompt prompt;

    @Column(name = "log_type", nullable = false, length = 50)
    private String logType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}