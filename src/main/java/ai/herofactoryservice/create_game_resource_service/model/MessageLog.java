package ai.herofactoryservice.create_game_resource_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_logs",
        indexes = {
                @Index(name = "idx_message_id_status", columnList = "messageId,status"),
                @Index(name = "idx_payment_id", columnList = "paymentId")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String messageId;

    @Column(nullable = false)
    private String paymentId;

    @Column(nullable = false)
    private String status;  // SENDING, SENT, FAILED, PROCESSING, PROCESSED

    @Column(length = 500)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;
}