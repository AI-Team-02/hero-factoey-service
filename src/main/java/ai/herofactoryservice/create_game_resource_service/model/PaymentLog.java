package ai.herofactoryservice.create_game_resource_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_logs",
        indexes = {
                @Index(name = "idx_payment_id_created_at", columnList = "payment_id,created_at")
        })
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private String logType;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}