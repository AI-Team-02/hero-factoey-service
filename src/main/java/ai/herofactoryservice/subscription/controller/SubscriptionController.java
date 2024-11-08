package ai.herofactoryservice.subscription.controller;

import ai.herofactoryservice.subscription.dto.request.SubscriptionCancelRequest;
import ai.herofactoryservice.subscription.dto.request.SubscriptionRequest;
import ai.herofactoryservice.subscription.dto.response.SubscriptionResponse;
import ai.herofactoryservice.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody SubscriptionRequest request) {
        log.info("Subscription creation request received: {}", request);
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{subscriptionId}/renew")
    public ResponseEntity<SubscriptionResponse> renewSubscription(
            @PathVariable String subscriptionId) {
        log.info("Subscription renewal request received for subscriptionId: {}", subscriptionId);
        SubscriptionResponse response = subscriptionService.renewSubscription(subscriptionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @PathVariable String subscriptionId,
            @Valid @RequestBody SubscriptionCancelRequest request) {
        log.info("Subscription cancellation request received for subscriptionId: {}", subscriptionId);
        SubscriptionResponse response = subscriptionService.cancelSubscription(subscriptionId, request.getReason());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{subscriptionId}/reactivate")
    public ResponseEntity<SubscriptionResponse> reactivateSubscription(
            @PathVariable String subscriptionId) {
        log.info("Subscription reactivation request received for subscriptionId: {}", subscriptionId);
        SubscriptionResponse response = subscriptionService.reactivateSubscription(subscriptionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<SubscriptionResponse>> getMemberSubscriptions(
            @PathVariable String memberId) {
        log.info("Fetching subscriptions for memberId: {}", memberId);
        List<SubscriptionResponse> subscriptions = subscriptionService.getMemberSubscriptions(memberId);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> getSubscription(
            @PathVariable String subscriptionId) {
        log.info("Fetching subscription details for subscriptionId: {}", subscriptionId);
        SubscriptionResponse subscription = subscriptionService.getSubscriptionStatus(subscriptionId); // getSubscription 대신 getSubscriptionStatus 사용
        return ResponseEntity.ok(subscription);
    }
}
