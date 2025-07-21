package com.insidemovie.backend.api.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmdbApiSearchMovieService {
    private final RestTemplate restTemplate;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.language}")
    private String language;

    @Value("${tmdb.image.base-url}")
    private String imageBaseUrl;

    @Value("${tmdb.image.poster-size}")
    private String posterSize;


}
