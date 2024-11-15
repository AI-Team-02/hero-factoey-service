//package ai.herofactoryservice.subscription.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.retry.annotation.Retryable;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.concurrent.CompletableFuture;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SmsService {
//    private final RestTemplate restTemplate;
//
//    @Value("${sms.api.url}")
//    private String apiUrl;
//
//    @Value("${sms.api.key}")
//    private String apiKey;
//
//    @Value("${sms.sender.number}")
//    private String senderNumber;
//
//    @Async
//    @Retryable(
//            value = {Exception.class},
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 1000, multiplier = 2)
//    )
//    public CompletableFuture<Void> sendSms(String recipientId, String content) {
//        return CompletableFuture.runAsync(() -> {
//            try {
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_JSON);
//                headers.set("Authorization", "Bearer " + apiKey);
//
//                Map<String, Object> requestBody = new HashMap<>();
//                requestBody.put("recipientNumber", recipientId);
//                requestBody.put("content", content);
//                requestBody.put("senderNumber", senderNumber);
//
//                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
//
//                ResponseEntity<Map> response = restTemplate.exchange(
//                        apiUrl,
//                        HttpMethod.POST,
//                        request,
//                        Map.class
//                );
//
//                if (response.getStatusCode() == HttpStatus.OK) {
//                    log.info("SMS sent successfully to: {}", recipientId);
//                } else {
//                    log.error("Failed to send SMS. Status: {}, Response: {}",
//                            response.getStatusCode(), response.getBody());
//                    throw new RuntimeException("SMS sending failed");
//                }
//
//            } catch (Exception e) {
//                log.error("Error sending SMS to: {}, reason: {}", recipientId, e.getMessage());
//                throw e;
//            }
//        });
//    }
//}