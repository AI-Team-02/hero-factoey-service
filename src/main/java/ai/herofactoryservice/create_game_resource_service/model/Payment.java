package ai.herofactoryservice.create_game_resource_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentId;    // UUID로 생성되는 고유 ID
    private String orderId;      // 주문 ID
    private String tid;          // 카카오페이 거래 ID
    private Long shopItemId;     // 상품 ID
    private Long memberId;       // 회원 ID
    private Long amount;         // 결제 금액
    private String itemName;     // 상품명

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime canceledAt;

    private Long cancelAmount;    // 취소 금액
    private String cancelReason;  // 취소 사유
    private String paymentKey;    // 결제 키
    private String errorMessage;  // 에러 메시지
}