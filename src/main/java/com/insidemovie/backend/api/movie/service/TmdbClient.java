package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.dto.GenreDto;
import com.insidemovie.backend.api.movie.dto.GenreResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbClient {
    private final RestTemplate restTemplate;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.language}")
    private String language;

    public List<GenreDto> fetchAllGenres(){ //전체 장르 목록 조히
        String url = String.format("%s/genre/movie/list?api_key=%s&language=ko",
                baseUrl, apiKey,language);
        ResponseEntity<GenreResponse> response =
                restTemplate.getForEntity(url, GenreResponse.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            GenreResponse resp = response.getBody();
            return (resp != null)
                    ? resp.getGenres()
                    : List.of();
        } else {
            log.error("장르 저장 에러 : "+ List.of());
            return List.of();
        }
    }
}
