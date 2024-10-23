//package ai.herofactoryservice.create_game_resource_service.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ai.herofactoryservice.create_game_resource_service.messaging.GameResourceProducer;
//import ai.herofactoryservice.create_game_resource_service.model.*;
//import ai.herofactoryservice.create_game_resource_service.repository.GameResourceRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Base64;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class GameResourceService {
//    private final GameResourceProducer producer;
//    private final ObjectMapper objectMapper;
//    private final GameResourceRepository gameResourceRepository;
//
//    @Transactional
//    public String initiateGameResourceCreation(MultipartFile file, String prompt, Member member) throws Exception {
//        String taskId = UUID.randomUUID().toString();
//        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
//
//        GameResourceCreationMessage message = GameResourceCreationMessage.builder()
//                .taskId(taskId)
//                .fileName(file.getOriginalFilename())
//                .fileContent(base64Image)
//                .prompt(prompt)
//                .member(member)
//                .build();
//
//        gameResourceRepository.save(message);
//
//        producer.sendMessage(objectMapper.writeValueAsString(message));
//
//        return taskId;
//    }
//
//    @Transactional(readOnly = true)
//    public Map<String, Object> getTaskStatus(String taskId) {
//        GameResourceCreationMessage resource = gameResourceRepository.findByTaskId(taskId)
//                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
//
//        return Map.of(
//                "status", resource.getStatus(),
//                "prompt", resource.getPrompt(),
//                "fileName", resource.getFileName(),
//                "createdAt", resource.getCreatedAt()
//        );
//    }
//
//    @Transactional
//    public void updateTaskStatus(String taskId, Map<String, Object> newStatus) {
//        GameResourceCreationMessage resource = gameResourceRepository.findByTaskId(taskId)
//                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
//
//        if (newStatus.containsKey("status")) {
//            resource.setStatus(ResourceStatus.valueOf(newStatus.get("status").toString().toUpperCase()));
//        }
//
//        // 다른 상태 업데이트 로직 추가 가능
//        gameResourceRepository.save(resource);
//        log.info("Task status updated for taskId: {}, status: {}", taskId, resource.getStatus());
//    }
//
//    @Transactional(readOnly = true)
//    public List<GameResourceCreationMessage> getUserResources(Long memberId) {
//        return gameResourceRepository.findByMemberId(memberId);
//    }
//}