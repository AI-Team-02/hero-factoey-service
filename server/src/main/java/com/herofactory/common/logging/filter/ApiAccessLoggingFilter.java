package com.herofactory.common.logging.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiAccessLoggingFilter extends OncePerRequestFilter {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        long startTime = System.currentTimeMillis();
        String path = request.getRequestURI();
        Map<String, Object> additionalInfo = new HashMap<>();
        String errorMessage = null;

        if (path.equals("/api/items/search")) {
            String keyword = request.getParameter("keyword");
            if (keyword != null) {

                String decodedKeyword = URLDecoder.decode(keyword, StandardCharsets.UTF_8);
                additionalInfo.put("searchKeyword", decodedKeyword);
            }
        }

        try {
            filterChain.doFilter(request, responseWrapper);


            if (path.matches("/shop/items/\\d+")) {
                String itemId = path.substring(path.lastIndexOf("/") + 1);

                // 응답 데이터 읽기
                String responseBody = new String(responseWrapper.getContentAsByteArray());

                if (responseBody != null && !responseBody.isEmpty()) {
                    JsonNode jsonNode = objectMapper.readTree(responseBody);
                    String itemName = jsonNode.path("name").asText();

                    additionalInfo.put("itemId", itemId);
                    additionalInfo.put("itemName", itemName);
                }
            }
        } catch (Exception e) {
            errorMessage = e.getMessage();
            throw e;
        } finally {
            responseWrapper.copyBodyToResponse();
            try {
                // 로그 데이터 생성
                Map<String, Object> logData = new HashMap<>();

                // 기본 정보
                logData.put("loggedAt", ZonedDateTime.now(ZoneId.systemDefault()).format(FORMATTER));
                logData.put("path", request.getRequestURI());
                logData.put("method", request.getMethod());
                logData.put("status", response.getStatus());
                logData.put("responseTime", System.currentTimeMillis() - startTime);

                // 클라이언트 정보
                logData.put("clientIp", getClientIp(request));
                logData.put("userAgent", request.getHeader("User-Agent"));

                // 인증 정보
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof Long) {
                    logData.put("userId", auth.getPrincipal());
                }

                // 추가 정보
                logData.put("queryString", request.getQueryString());
                logData.put("referer", request.getHeader("Referer"));

                // 에러 정보
                if (errorMessage != null) {
                    logData.put("errorMessage", errorMessage);
                }

                if (!additionalInfo.isEmpty()) {
                    logData.put("additionalInfo", additionalInfo);
                }

                // Kafka로 전송
                String logMessage = objectMapper.writeValueAsString(logData);
                kafkaTemplate.send("api-access-log", logMessage)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send log to Kafka", ex);
                            }
                        });

            } catch (Exception e) {
                log.error("Error while logging API access", e);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For may contain multiple IPs, get the first one
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/kakao/") ||
                path.startsWith("/images/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/auth/reissue") ||
                path.startsWith("/test");
    }
}