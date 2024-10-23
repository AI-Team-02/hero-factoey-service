//package ai.herofactoryservice.create_game_resource_service.repository;
//
//import ai.herofactoryservice.create_game_resource_service.model.Member;
//import ai.herofactoryservice.create_game_resource_service.model.Provider;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//
//@Repository
//public interface MemberRepository extends JpaRepository<Member, Long> {
//    Optional<Member> findByEmail(String email);
//    Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);
//    boolean existsByEmail(String email);
//}