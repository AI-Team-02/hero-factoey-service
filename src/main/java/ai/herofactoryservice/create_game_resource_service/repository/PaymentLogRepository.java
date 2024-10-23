package ai.herofactoryservice.create_game_resource_service.repository;

import ai.herofactoryservice.create_game_resource_service.model.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {
}