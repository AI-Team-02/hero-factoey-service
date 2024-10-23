//package ai.herofactoryservice.create_game_resource_service.repository;
//
//import ai.herofactoryservice.create_game_resource_service.model.GameResourceCreationMessage;
//import ai.herofactoryservice.create_game_resource_service.model.ResourceStatus;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//import java.util.List;
//
//@Repository
//public interface GameResourceRepository extends JpaRepository<GameResourceCreationMessage, Long> {
//    Optional<GameResourceCreationMessage> findByTaskId(String taskId);
//    List<GameResourceCreationMessage> findByMemberId(Long memberId);
//    List<GameResourceCreationMessage> findByStatus(ResourceStatus status);
//}