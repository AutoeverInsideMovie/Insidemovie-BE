package com.insidemovie.backend.api.movie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.awt.*;

@Configuration
public class WebClientConfig {
    @Value("${kobis.api.base-url}")
    private String kobisBaseUrl;

    @Bean
    public WebClient kobisWebClient(){
        return WebClient.builder()
                .baseUrl(kobisBaseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
