package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.constant.MovieLanguage;
import com.insidemovie.backend.api.movie.dto.tmdb.*;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.GenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;
    private final GenreRepository genreRepository;
    private final MovieGenreRepository movieGenreRepository;

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

    @Transactional
    public void fetchAndSaveMoviesByPage(String type, int page, boolean isInitial) {
        // 목록 조회
        String url = String.format("%s/movie/%s?api_key=%s&language=%s&page=%d",
                baseUrl, type, apiKey, language, page);

        ResponseEntity<TmdbResponse> response =
                restTemplate.getForEntity(url, TmdbResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return;
        }

        for (TmdbMovieResponseDTO dto : response.getBody().getResults()) {
            if (!MovieLanguage.isAllowed(dto.getOriginalLanguage())) {
                log.info("[필터링] 지원하지 않는 언어({}) 영화(ID={}) 건너뜀",
                        dto.getOriginalLanguage(), dto.getId());
                continue;
            }

            if (dto.getAdult() != null && dto.getAdult()) {
                    log.info("성인 영화 스킵: " + dto.getId() + " - " + dto.getTitle());
                    continue;
            }

            log.info("처리 시작: ID={} / {}", dto.getId(), dto.getTitle());

            // 상세 정보(credits, release_dates, watch/providers) 한번에 조회
            String detailUrl = String.format(
                "%s/movie/%d?api_key=%s&language=%s&append_to_response=credits,release_dates,watch/providers",
                baseUrl, dto.getId(), apiKey, language
            );
            ResponseEntity<MovieDetailDTO> detailRes =
                    restTemplate.getForEntity(detailUrl, MovieDetailDTO.class);
            if (!detailRes.getStatusCode().is2xxSuccessful() || detailRes.getBody() == null) {
                log.warn("상세정보 조회 실패: ID={}", dto.getId());
                continue;
            }
            MovieDetailDTO detail = detailRes.getBody();

            // 기존 엔티티 조회 또는 신규 생성
            Optional<Movie> optional = movieRepository.findByTmdbMovieId(dto.getId());
            Movie movie = optional.orElseGet(() -> Movie.builder()
                    .tmdbMovieId(dto.getId())
                    .build()
            );

            String fullPosterUrl = imageBaseUrl+posterSize+movie.getPosterPath();
            String fullBackDropUrl = imageBaseUrl+posterSize+movie.getBackdropPath();
            double raw = movie.getVoteAverage();  // 예: 6.2309999…
            double rounded = BigDecimal.valueOf(raw)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

            // 기본 정보 갱신
            movie.updateTitle(dto.getTitle());
            movie.updateOverview(dto.getOverview());
            movie.updatePosterPath(fullPosterUrl);
            movie.updateBackDropPath(fullBackDropUrl);
            movie.updateVoteAverage(rounded);
            movie.updateOriginalLanguage(dto.getOriginalLanguage());
            movie.updateReleaseDate(dto.getReleaseDate());
            movie.updateGenreIds(dto.getGenreIds());
            movie.setTitleEn(detail.getOriginalTitle());
            movie.updatePopularity(dto.getPopularity());

            // 추가 정보 매핑
            // 배우
            List<String> actors = detail.getCredits().getCast().stream()
                    .map(CastDTO::getName)
                    .collect(Collectors.toList());
            movie.setActors(actors.toString());

            // 감독
            List<String> directors = detail.getCredits().getCrew().stream()
                    .filter(c -> "Director".equals(c.getJob()))
                    .map(CrewDTO::getName)
                    .collect(Collectors.toList());
            movie.setDirectors(directors.toString());

            // 러닝타임, 상태, 투표수
            movie.setRuntime(detail.getRuntime());
            movie.setStatus(detail.getStatus());
            movie.setVoteCount(detail.getVoteCount());

            // 등급 (KR 기준 첫 번째 certification)
            String rating = detail.getReleaseDates().getResults().stream()
                    .filter(r -> "KR".equals(r.getIso3166_1()))
                    .flatMap(r -> r.getReleaseDates().stream())
                    .map(ReleaseDateDTO::getCertification)
                    .filter(cert -> cert != null && !cert.isEmpty())
                    .findFirst()
                    .orElse(null);
            movie.setRating(rating);

            // OTT 제공처 (KR flatrate)
            List<String> ott = Optional.ofNullable(detail.getWatchProviders()
                    .getResults().get("KR"))
                    .map(cp -> Optional.ofNullable(cp.getFlatrate()).orElse(Collections.emptyList())
                    .stream()
                        .map(ProviderDTO::getProviderName)
                        .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());

            movie.setOttProviders(ott.toString());
            movieRepository.save(movie);
            log.info("저장 완료: ID={}", dto.getId());
        }
    }

    private boolean hasChanged(Movie movie, TmdbMovieResponseDTO dto) {
        return !Objects.equals(movie.getTitle(), dto.getTitle())
                || !Objects.equals(movie.getOverview(), dto.getOverview())
                || !Objects.equals(movie.getPosterPath(), dto.getPosterPath())
                || !Objects.equals(movie.getBackdropPath(), dto.getBackDropPath())
                || !Objects.equals(movie.getVoteAverage(), dto.getVoteAverage())
                || !Objects.equals(movie.getGenreIds(), dto.getGenreIds())
                || !Objects.equals(movie.getOriginalLanguage(), dto.getOriginalLanguage())
                || !Objects.equals(movie.getReleaseDate(),
                dto.getReleaseDate() != null ? dto.getReleaseDate() : null);
    }
}
