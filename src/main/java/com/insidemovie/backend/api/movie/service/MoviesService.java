package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.dto.MovieDetail;
import com.insidemovie.backend.api.movie.dto.PaginatedResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
@Service
public class MoviesService {
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private final String language;

    public MoviesService(RestTemplate restTemplate,
                         @Value("${tmdb.api.key}") String apiKey,
                         @Value("${tmdb.api.base-url}") String baseUrl,
                         @Value("${tmdb.api.language}") String language) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.language= language;
    }

    /** 영화 ID로 상세 정보 조회 */
    public MovieDetail getMovieDetail(String movieId) {
        String url = String.format("%s/movie/%s?api_key=%s&language=%s", baseUrl, movieId, apiKey, language);
        return restTemplate.getForObject(url, MovieDetail.class);
    }

    /** 인기 영화 목록 조회 (한국어) */
    public List<MovieDetail> getPopularMovies(int page) {
        String url = String.format(
                "%s/movie/popular?api_key=%s&language=%s&page=%d",
                baseUrl, apiKey, language,page
        );
        PaginatedResponse<MovieDetail> resp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginatedResponse<MovieDetail>>() {}
        ).getBody();

        return (resp != null) ? resp.getResults() : Collections.emptyList();
    }

}
