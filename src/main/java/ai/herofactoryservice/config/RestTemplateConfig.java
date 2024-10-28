package ai.herofactoryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Configure timeouts
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
//        requestFactory.setConnectTimeout(5000); // 연결 타임아웃 5초
//        requestFactory.setReadTimeout(30000);   // 읽기 타임아웃 30초

        restTemplate.setRequestFactory(requestFactory);

        return restTemplate;
    }
}