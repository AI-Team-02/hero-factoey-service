package ai.herofactoryservice.payment.repository;

import ai.herofactoryservice.payment.entity.Payment;
import ai.herofactoryservice.payment.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentId(String paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId")
    Optional<Payment> findByPaymentIdWithLock(@Param("paymentId") String paymentId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    List<Payment> findByStatusWithLock(@Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.memberId = :memberId ORDER BY p.createdAt DESC")
    Page<Payment> findByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.status = :status " +
            "AND p.createdAt < :timeThreshold")
    List<Payment> findExpiredPayments(@Param("status") PaymentStatus status,
                                      @Param("timeThreshold") LocalDateTime timeThreshold);

    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.paymentId = :paymentId " +
            "AND p.status = :status")
    boolean existsByPaymentIdAndStatus(@Param("paymentId") String paymentId,
                                       @Param("status") PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.status = :status " +
            "AND p.updatedAt < :cutoffTime " +
            "ORDER BY p.createdAt ASC")
    List<Payment> findStalePayments(@Param("status") PaymentStatus status,
                                    @Param("cutoffTime") LocalDateTime cutoffTime,
                                    Pageable pageable);
}