package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.dto.tmdb.TmdbMovieDto;
import com.insidemovie.backend.api.movie.dto.tmdb.TmdbResponse;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
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
        List<TmdbMovieDto> allDtos = new ArrayList<>();
        for (int page = 1; page <= 5; page++) {
            allDtos.addAll(fetchFromTmdb(page));
        }
        Map<Long, TmdbMovieDto> dtoMap = allDtos.stream()
                .collect(Collectors.toMap(
                        TmdbMovieDto::getId,
                        Function.identity(),
                        (dto1,dto2)->dto1
                ));
        List<Long> ids = new ArrayList<>(dtoMap.keySet());
        List<Movie> existing = movieRepository.findAllByTmdbMovieIdIn(ids);
        Map<Long, Movie> existingMap = existing.stream()
                .collect(Collectors.toMap(Movie::getTmdbMovieId,Function.identity()));

        existingMap.forEach((tmdbId, movie)->{
            TmdbMovieDto dto = dtoMap.get(tmdbId);
            movie.updateTitle(dto.getTitle());
            movie.updateOverview(dto.getOverview());
            movie.updatePosterPath(dto.getPosterPath());
            movie.updateBackDropPath(dto.getBackDropPath());
            movie.updateVoteAverage(dto.getVoteAverage());
            movie.updateReleaseDate(dto.getReleaseDate());
            movie.updateGenreIds(dto.getGenreIds());
            movie.updateOriginalLanguage(dto.getOriginalLanguage());
        });
        List<Movie> newMovies = dtoMap.entrySet().stream()
                .filter(e -> !existingMap.containsKey(e.getKey()))
                .map(e -> {
                    TmdbMovieDto dto = e.getValue();
                    Movie m = Movie.builder()
                            .tmdbMovieId(dto.getId())
                            .build();
                    // 역시 updateXxx 호출
                    m.updateTitle(dto.getTitle());
                    m.updateOverview(dto.getOverview());
                    // ... 나머지 updateXxx
                    return m;
                })
                .toList();

        // 6) 신규 영화만 한 번에 저장
        movieRepository.saveAll(newMovies);

    }
    private List<TmdbMovieDto> fetchFromTmdb(int page) {
        List<TmdbMovieDto> all = new ArrayList<>();
        String[] categories ={
                "popular",
                "now_playing",
                "top_rated",
                "upcoming"
        };
        for (String cat : categories) {
            String url = String.format(
                    "%s/movie/%s?api_key=%s&language=%s&page=%d",
                    baseUrl, cat, apiKey, language, page
            );
            ResponseEntity<TmdbResponse> resp =
                    restTemplate.getForEntity(url, TmdbResponse.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                //return resp.getBody().getResults();  // DTO 리스트 반환
                all.addAll(resp.getBody().getResults());
            } else {
                // 실패 시 빈 리스트 반환 또는 예외 처리
                //return Collections.emptyList();
                log.warn("TMDB {} page {} 호출 실패: {}", cat, page, resp.getStatusCode());
            }
        }
       return all.stream()
                .collect(Collectors.toMap(
                        TmdbMovieDto::getId,
                        Function.identity(),
                        (existing, replacement) -> existing))
                .values()
                .stream()
                .toList();

    }

}
