//package ai.herofactoryservice.create_game_resource_service.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Map;
//
//@Slf4j
//@Service
//public class FastAPIClientService {
//
//    @Value("${comfyui.server.url}")
//    private String comfyuiServerUrl;
//
//    @Value("${comfyui.server.port}")
//    private String comfyuiServerPort;
//
//    private final RestTemplate restTemplate;
//
//
//    public FastAPIClientService(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    public Map<String, Object> generateImage(MultipartFile file, String prompt) throws Exception {
//        String url = String.format("http://%s:%s/generate", comfyuiServerUrl, comfyuiServerPort);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("file", new ByteArrayResource(file.getBytes()) {
//            @Override
//            public String getFilename() {
//                return file.getOriginalFilename();
//            }
//        });
//        body.add("prompt", prompt);
//
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
//
//        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.ACCEPTED) {
//            return response.getBody();
//        } else {
//            throw new RuntimeException("Failed to generate image: " + response.getBody());
//        }
//    }
//
//    public Map<String, Object> getTaskStatus(String taskId) {
//        String url = String.format("http://%s:%s/status/%s", comfyuiServerUrl, comfyuiServerPort, taskId);
//
//        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
//        if (response.getStatusCode() == HttpStatus.OK) {
//            log.info("Retrieved status for task: {}. Status: {}", taskId, response.getBody());
//            return response.getBody();
//        } else {
//            log.error("Failed to get task status. Task ID: {}, Status code: {}, Response: {}", taskId, response.getStatusCode(), response.getBody());
//            throw new RuntimeException("Failed to get task status: " + response.getBody());
//        }
//    }
//}