package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.dto.MovieDetail;
import com.insidemovie.backend.api.movie.dto.PaginatedResponse;
import com.insidemovie.backend.api.movie.dto.TmdbMovieDto;
import com.insidemovie.backend.api.movie.dto.TmdbResponse;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MoviesService {
    private final RestTemplate restTemplate;

    private final MovieRepository movieRepository;
    // application.yml 에서 읽어오기
    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.language}")
    private String language;

    public MoviesService(RestTemplateBuilder builder, MovieRepository movieRepository) {
        this.restTemplate = builder.build();
        this.movieRepository = movieRepository;
    }

    @Transactional
    public void fetchAndSaveAllMovies() {
        for (int page = 1; page <= 5; page++) {
            List<TmdbMovieDto> dtos = fetchFromTmdb(page);
            dtos.forEach(this::upsertMovie);
        }
    }
    private List<TmdbMovieDto> fetchFromTmdb(int page) {
        String url = String.format(
                "%s/movie/popular?api_key=%s&language=%s&page=%d",
                baseUrl, apiKey, language, page
        );
        ResponseEntity<TmdbResponse> resp =
                restTemplate.getForEntity(url, TmdbResponse.class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            return resp.getBody().getResults();  // DTO 리스트 반환
        } else {
            // 실패 시 빈 리스트 반환 또는 예외 처리
            return Collections.emptyList();
        }

    }
    @Transactional
    public void upsertMovie(TmdbMovieDto dto) {
        Movie movie = movieRepository
                .findByTmdbMovieId(dto.getId())
                .orElseGet(() -> Movie.builder()
                        .tmdbMovieId(dto.getId())
                        .build());

        // 엔티티의 update 메서드를 호출해서 필드 갱신
        movie.updateTitle(dto.getTitle());
        movie.updateOverview(dto.getOverview());
        movie.updateReleaseDate(dto.getReleaseDate());
        // …필요한 다른 필드도

        movieRepository.save(movie);
    }
    private Movie toEntity(TmdbMovieDto dto) {
        return Movie.builder()
                .id(dto.getId())             // TMDB movie id 를 엔티티의 PK 로 사용
                .title(dto.getTitle())
                .posterPath(dto.getPosterPath())
                .rating(dto.getVoteAverage())
                .releaseDate(dto.getReleaseDate())
                .cachedAt(LocalDateTime.now())
                .build();
    }
//
//    public MoviesService(RestTemplate restTemplate,
//                         @Value("${tmdb.api.key}") String apiKey,
//                         @Value("${tmdb.api.base-url}") String baseUrl,
//                         @Value("${tmdb.api.language}") String language) {
//        this.restTemplate = restTemplate;
//        this.apiKey = apiKey;
//        this.baseUrl = baseUrl;
//        this.language= language;
//    }
//
//    /** 영화 ID로 상세 정보 조회 */
//    public MovieDetail getMovieDetail(String movieId) {
//        String url = String.format("%s/movie/%s?api_key=%s&language=%s", baseUrl, movieId, apiKey, language);
//        return restTemplate.getForObject(url, MovieDetail.class);
//    }
//
//    /** 인기 영화 목록 조회 (한국어) */
//    public List<MovieDetail> getPopularMovies(int page) {
//        String url = String.format(
//                "%s/movie/popular?api_key=%s&language=%s&page=%d",
//                baseUrl, apiKey, language,page
//        );
//        PaginatedResponse<MovieDetail> resp = restTemplate.exchange(
//                url,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<PaginatedResponse<MovieDetail>>() {}
//        ).getBody();
//
//        return (resp != null) ? resp.getResults() : Collections.emptyList();
//    }

}
