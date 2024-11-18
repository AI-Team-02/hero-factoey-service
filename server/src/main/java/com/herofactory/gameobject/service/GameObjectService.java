package com.herofactory.gameobject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameObjectService {
    private final String FAST_API_URL = "http://localhost:8000/generate-image";
    private final RestTemplate restTemplate;

    public byte[] sendImageToFastAPI(MultipartFile file, String prompt) throws IOException {
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Create the multipart request body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("prompt", prompt);

        // Create the HTTP entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        try {
            // Send request to FastAPI
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    FAST_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to process image: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error communicating with FastAPI server: ", e);
            throw new RuntimeException("Failed to communicate with image processing server", e);
        }
    }
}

