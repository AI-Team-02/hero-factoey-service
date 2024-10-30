package ai.herofactoryservice.create_game_resource_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments",
        indexes = {
                @Index(name = "idx_payment_id", columnList = "paymentId"),
                @Index(name = "idx_order_id", columnList = "orderId"),
                @Index(name = "idx_member_id", columnList = "memberId"),
                @Index(name = "idx_status", columnList = "status")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String paymentId;    // UUID로 생성되는 고유 ID

    @Column(nullable = false)
    private String orderId;      // 주문 ID

    @Column(nullable = false)
    private String tid;          // 카카오페이 거래 ID

    @Column(nullable = false)
    private Long shopItemId;     // 상품 ID

    @Column(nullable = false)
    private String memberId;       // 회원 ID

    @Column(nullable = false)
    private Long amount;         // 결제 금액

    @Column(nullable = false)
    private String itemName;     // 상품명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime canceledAt;

    private Long cancelAmount;    // 취소 금액
    private String cancelReason;  // 취소 사유

    @Column(length = 100)
    private String paymentKey;    // 결제 키

    @Column(length = 500)
    private String errorMessage;  // 에러 메시지
}