//package ai.herofactoryservice.create_game_resource_service.controller;
//
//import ai.herofactoryservice.create_game_resource_service.service.FastAPIClientService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/comfyui")
//public class ComfyUIConnectionTestController {
//
//    private final FastAPIClientService fastAPIClientService;
//
//    @Autowired
//    public ComfyUIConnectionTestController(FastAPIClientService fastAPIClientService) {
//        this.fastAPIClientService = fastAPIClientService;
//    }
//
//    @GetMapping("/test-connection")
//    public ResponseEntity<String> testConnection() {
//        try {
//            Map<String, Object> response = fastAPIClientService.testConnection();
//            log.info("ComfyUI connection test response: {}", response);
//            return ResponseEntity.ok("Connection to ComfyUI successful. Response: " + response);
//        } catch (Exception e) {
//            log.error("Error connecting to ComfyUI", e);
//            return ResponseEntity.status(500).body("Error connecting to ComfyUI: " + e.getMessage());
//        }
//    }
//}