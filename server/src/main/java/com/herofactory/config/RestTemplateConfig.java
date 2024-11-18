package com.herofactory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Configuration
@Slf4j
public class RestTemplateConfig {
    @Value("${openai.api.request.timeout:60000}")
    private int openaiTimeout;

    @Value("${openai.api.request.connect-timeout:30000}")
    private int openaiConnectTimeout;

    @Value("${hikari.conn-timeout:30000}")
    private int defaultTimeout;

    @Bean
    @Primary
    public RestTemplate defaultRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(defaultTimeout))
                .setReadTimeout(Duration.ofMillis(defaultTimeout))
                .build();

        log.info("Configured default RestTemplate with timeout: {}ms", defaultTimeout);
        return restTemplate;
    }

    @Bean(name = "openAiRestTemplate")
    public RestTemplate openAiRestTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(openaiConnectTimeout))
                .setReadTimeout(Duration.ofMillis(openaiTimeout))
                .build();

        log.info("Configured OpenAI RestTemplate with connect timeout: {}ms, read timeout: {}ms",
                openaiConnectTimeout, openaiTimeout);
        return restTemplate;
    }
}