package ai.herofactoryservice.create_game_resource_service.repository;

import ai.herofactoryservice.create_game_resource_service.model.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {

    boolean existsByMessageIdAndStatus(String messageId, String status);

    Optional<MessageLog> findTopByMessageIdOrderByCreatedAtDesc(String messageId);

    @Query("SELECT ml FROM MessageLog ml WHERE ml.paymentId = :paymentId ORDER BY ml.createdAt DESC")
    List<MessageLog> findMessageLogsByPaymentId(@Param("paymentId") String paymentId);

    @Query("SELECT ml FROM MessageLog ml WHERE ml.status = :status AND ml.createdAt < :cutoffTime")
    List<MessageLog> findStaleMessages(@Param("status") String status,
                                       @Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(ml) FROM MessageLog ml WHERE ml.messageId = :messageId AND ml.status = 'FAILED'")
    int countFailedAttempts(@Param("messageId") String messageId);
}