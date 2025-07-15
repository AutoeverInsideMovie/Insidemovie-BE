package com.insidemovie.backend.api.movie.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                // 연결 시도 최대 5초
                .connectTimeout(Duration.ofSeconds(5))
                // 응답 대기 최대 5초
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }
}