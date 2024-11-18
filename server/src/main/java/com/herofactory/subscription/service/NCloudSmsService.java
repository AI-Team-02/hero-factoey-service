package com.herofactory.subscription.service;

import com.herofactory.subscription.dto.request.SmsRequest;
import com.herofactory.subscription.dto.response.SmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
//@Service
@RequiredArgsConstructor
public class NCloudSmsService {
    private final RestTemplate restTemplate;

    @Value("${spring.cloud.ncp.sms.api.url}")
    private String apiUrl;

    @Value("${spring.cloud.ncp.sms.api.service-id}")
    private String serviceId;

    @Value("${spring.cloud.ncp.sms.api.access-key}")
    private String accessKey;

    @Value("${spring.cloud.ncp.sms.api.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.ncp.sms.sender.number}")
    private String senderNumber;

    @Async
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CompletableFuture<Void> sendSms(String recipientNumber, String content) {
        return CompletableFuture.runAsync(() -> {
            try {
                String timestamp = String.valueOf(System.currentTimeMillis());
                HttpHeaders headers = createHeaders(timestamp);

                SmsRequest request = SmsRequest.builder()
                        .type("SMS")
                        .contentType("COMM")
                        .countryCode("82")
                        .from(senderNumber)
                        .content(content)
                        .messages(List.of(new SmsRequest.Message(recipientNumber)))
                        .build();

                HttpEntity<SmsRequest> entity = new HttpEntity<>(request, headers);

                ResponseEntity<SmsResponse> response = restTemplate.exchange(
                        apiUrl + "/services/" + serviceId + "/messages",
                        HttpMethod.POST,
                        entity,
                        SmsResponse.class
                );

                if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                    log.info("SMS sent successfully to: {}", recipientNumber);
                } else {
                    log.error("Failed to send SMS. Status: {}, Response: {}",
                            response.getStatusCode(), response.getBody());
                    throw new RuntimeException("SMS sending failed");
                }

            } catch (Exception e) {
                log.error("Error sending SMS to: {}, reason: {}", recipientNumber, e.getMessage());
                throw e;
            }
        });
    }

    private HttpHeaders createHeaders(String timestamp) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", timestamp);
        headers.set("x-ncp-iam-access-key", accessKey);
        headers.set("x-ncp-apigw-signature-v2", makeSignature(timestamp));
        return headers;
    }

    private String makeSignature(String timestamp) {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + serviceId + "/messages";

        String message = method +
                space +
                url +
                newLine +
                timestamp +
                newLine +
                accessKey;

        try {
            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to create signature", e);
        }
    }
}