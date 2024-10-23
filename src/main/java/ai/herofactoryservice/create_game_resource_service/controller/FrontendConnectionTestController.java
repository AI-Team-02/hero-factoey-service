//package ai.herofactoryservice.create_game_resource_service.controller;
//
//import ai.herofactoryservice.create_game_resource_service.service.GameResourceService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Profile;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/test")
//@Profile("frontend-test")
//public class FrontendConnectionTestController {
//
//    private final GameResourceService gameResourceService;
//
//    @Autowired
//    public FrontendConnectionTestController(GameResourceService gameResourceService) {
//        this.gameResourceService = gameResourceService;
//    }
//
//    @PostMapping("/create-resource")
//    public ResponseEntity<String> testCreateResource(@RequestParam("file") MultipartFile file,
//                                                     @RequestParam("prompt") String prompt) {
//        try {
//            String taskId = gameResourceService.initiateGameResourceCreation(file, prompt);
//            return ResponseEntity.ok("Resource creation initiated. Task ID: " + taskId);
//        } catch (Exception e) {
//            log.error("Error in test resource creation", e);
//            return ResponseEntity.status(500).body("Error: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/status/{taskId}")
//    public ResponseEntity<?> getTaskStatus(@PathVariable String taskId) {
//        try {
//            return ResponseEntity.ok(gameResourceService.getTaskStatus(taskId));
//        } catch (Exception e) {
//            log.error("Error getting task status", e);
//            return ResponseEntity.status(500).body("Error: " + e.getMessage());
//        }
//    }
//}