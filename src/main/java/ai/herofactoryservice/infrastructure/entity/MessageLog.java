package ai.herofactoryservice.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_logs", indexes = {
        @Index(name = "idx_message_status", columnList = "messageId,status"),
        @Index(name = "idx_payment", columnList = "paymentId"),
        @Index(name = "idx_prompt", columnList = "promptId"),
        @Index(name = "idx_message_composite", columnList = "messageId,status,promptId")
})
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String messageId;

    @Column(nullable = true)  // payment 또는 prompt 중 하나만 있을 수 있으므로
    private String paymentId;

    @Column(nullable = true)
    private String promptId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private String errorMessage;

    @Builder.Default
    private Integer retryCount = 0;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}