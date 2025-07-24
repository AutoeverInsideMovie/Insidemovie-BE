package com.insidemovie.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebClientConfig {
    @Bean
    public RestTemplate fastApiRestTemplate(RestTemplateBuilder builder) {
        // 배포
        // return builder.rootUri("http://52.79.175.149:8000").build();
        // 로컬
        return builder.rootUri("http://localhost:8000").build();

    }

    @Bean
    public RestTemplate kobisRestTemplate(
            RestTemplateBuilder builder,
            @Value("${kobis.api.base-url}") String kobisApiUrl
    ) {
        return builder.rootUri(kobisApiUrl).build();
    }
}
