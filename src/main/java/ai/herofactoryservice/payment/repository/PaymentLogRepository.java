package ai.herofactoryservice.payment.repository;

import ai.herofactoryservice.payment.entity.Payment;
import ai.herofactoryservice.payment.entity.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    @Query("SELECT p FROM PaymentLog p WHERE p.payment.paymentId = :paymentId ORDER BY p.createdAt DESC")
    List<PaymentLog> findByPaymentId(@Param("paymentId") String paymentId);

    @Query("SELECT p FROM PaymentLog p WHERE p.logType = :logType AND p.createdAt < :cutoffTime")
    List<PaymentLog> findOldLogs(@Param("logType") String logType,
                                 @Param("cutoffTime") LocalDateTime cutoffTime);
    @Query("SELECT COUNT(p) FROM PaymentLog p WHERE p.payment.id = :paymentId AND p.logType = :logType")
    long countByPaymentIdAndLogType(@Param("paymentId") Long paymentId, @Param("logType") String logType);
}