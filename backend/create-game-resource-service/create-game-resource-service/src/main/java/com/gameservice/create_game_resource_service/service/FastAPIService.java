//package com.gameservice.create_game_resource_service.service;
//
//import com.gameservice.create_game_resource_service.model.GameResourceCreationMessage;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//@Service
//public class FastAPIService {
//
//    @Value("${fastapi.url}")
//    private String fastApiUrl;
//
//    private final RestTemplate restTemplate;
//
//    public FastAPIService(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    public String generateImage(GameResourceCreationMessage message) {
//        // Implement the logic to send a request to FastAPI and get the generated image URL
//        // This is a placeholder implementation
//        String response = restTemplate.postForObject(fastApiUrl + "/generate", message, String.class);
//        return response; // Assume this is the URL of the generated image
//    }
//}