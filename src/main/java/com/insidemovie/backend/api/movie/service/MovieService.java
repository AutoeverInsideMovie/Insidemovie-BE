package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.movie.dto.TmdbMovieDto;
import com.insidemovie.backend.api.movie.dto.TmdbResponse;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.language}")
    private String language;

    public MovieService(RestTemplateBuilder builder, MovieRepository movieRepository) {
        this.restTemplate = builder.build();
        this.movieRepository = movieRepository;
    }
    @Transactional
    public void fetchAndSaveMoviesByPage(String type, int page, boolean isInitial) {
        String url = String.format("%s/movie/%s?api_key=%s&language=ko&page=%d",
                baseUrl, type, apiKey, page);
        ResponseEntity<TmdbResponse> response =
                restTemplate.getForEntity(url, TmdbResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<TmdbMovieDto> movies = response.getBody().getResults();

            for (TmdbMovieDto dto : movies) {
                log.info("✅ 시도하는 dto.getId() = " + dto.getId());
                Optional<Movie> existing = movieRepository.findByTmdbMovieId(dto.getId());
                log.info("🔍existing.isEmpty(): " + existing.isEmpty());
                if (existing.isEmpty()) {
                    log.info("🆕 새 영화 저장: " + dto.getId() + " - " + dto.getTitle());
                    Movie movie = Movie.builder()
                            .tmdbMovieId(dto.getId())
                            .title(dto.getTitle())
                            .overview(dto.getOverview())
                            .posterPath(dto.getPosterPath())
                            .backdropPath(dto.getBackDropPath())
                            .voteAverage(dto.getVoteAverage())
                            .originalLanguage(dto.getOriginalLanguage())
                            .releaseDate(dto.getReleaseDate() != null ? dto.getReleaseDate() : null)
                            .genreIds(dto.getGenreIds())
                            .build();
                    movieRepository.save(movie);
                } else if (isInitial || hasChanged(existing.get(), dto)) {
                    log.info("⚠️ 중복: " + dto.getId() + " - " + dto.getTitle());
                    Movie movie = existing.get();

                    movie.updateTitle(dto.getTitle());
                    movie.updateOverview(dto.getOverview());
                    movie.updatePosterPath(dto.getPosterPath());
                    movie.updateBackDropPath(dto.getBackDropPath());
                    movie.updateVoteAverage(dto.getVoteAverage());
                    movie.updateReleaseDate(dto.getReleaseDate() != null ? dto.getReleaseDate() : null);
                    movie.updateGenreIds(dto.getGenreIds());
                    movie.updateOriginalLanguage(dto.getOriginalLanguage());
                    movieRepository.save(movie);
                }

            }
        }
    }
    private boolean hasChanged(Movie movie, TmdbMovieDto dto) {
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

    public void seedMovies() {
        LocalDate fromDate = LocalDate.of(2000, 1, 1);
        LocalDate toDate = LocalDate.now();

        // 첫 페이지로 전체 페이지 수 조회
        ResponseEntity<TmdbResponse> firstResp = fetchMoviesPage(1, fromDate, toDate);
        TmdbResponse firstBody = firstResp.getBody();
        if (firstBody == null) {
            log.error("첫 페이지 응답이 없습니다. 시딩 과정을 종료합니다.");
            return;
        }

        int totalPages = firstBody.getTotalPages();
        int maxPage = Math.min(totalPages, 499);

        for (int page = 1; page <= maxPage; page++) {
            log.info("페이지 {} 조회를 시작합니다.", page);
            ResponseEntity<TmdbResponse> respEntity = fetchMoviesPage(page, fromDate, toDate);
            TmdbResponse resp = respEntity != null ? respEntity.getBody() : null;
            if (resp == null || resp.getResults() == null) {
                log.warn("페이지 {} 응답이 없거나 결과가 비어 있습니다. 시딩을 중단합니다.", page);
                break;
            }

            // 1) 날짜 필터, 2) DB 중복 검사(TMDB ID), 3) 엔티티 변환
            List<Movie> moviesToSave = resp.getResults().stream()
                .filter(dto -> {
                    LocalDate rd = dto.getReleaseDate();
                    return rd != null && !rd.isBefore(fromDate) && !rd.isAfter(toDate);
                })
                .filter(dto -> !movieRepository.existsByTmdbMovieId(dto.getId()))
                .map(this::toEntity)
                .collect(Collectors.toList());

            if (!moviesToSave.isEmpty()) {
                movieRepository.saveAll(moviesToSave);
                log.info("페이지 {} 저장 완료 (저장 건수: {})", page, moviesToSave.size());
            } else {
                log.info("페이지 {}에 저장할 신규 영화가 없습니다.", page);
            }

            long totalCount = movieRepository.count();
            log.info("현재 DB에 저장된 영화 총 개수: {}", totalCount);
        }

        log.info("초기 시딩을 완료했습니다. 처리된 페이지 범위: 1~{}.", maxPage);
    }


    // TMDB Discover API 로 한 페이지의 데이터를 가져옵니다.
    public ResponseEntity<TmdbResponse> fetchMoviesPage(int page, LocalDate from, LocalDate to) {
        String url = UriComponentsBuilder
            .fromHttpUrl(baseUrl + "/discover/movie")
            .queryParam("api_key", apiKey)
            .queryParam("language", language)
            .queryParam("primary_release_date.gte", from.toString())
            .queryParam("primary_release_date.lte", to.toString())
            .queryParam("sort_by", "primary_release_date.desc")
            .queryParam("page", page)
            .build()
            .toUriString();

        try {
            return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<TmdbResponse>() {}
            );
        } catch (Exception e) {
            log.error("TMDB API 호출 실패 (page={}): {}", page, e.getMessage());
            return null;
        }
    }

    private Movie toEntity(TmdbMovieDto dto) {
        if (dto.getReleaseDate() == null) {
            return null;
        }
        return Movie.builder()
                .tmdbMovieId(dto.getId())
                .title(dto.getTitle())
                .overview(dto.getOverview())
                .posterPath(dto.getPosterPath())
                .backdropPath(dto.getBackDropPath())
                .voteAverage(dto.getVoteAverage())
                .releaseDate(dto.getReleaseDate())
                .originalLanguage(dto.getOriginalLanguage())
                .genreIds(dto.getGenreIds())
                .build();
    }

    // 전체 영화 페이징 조회
    @Transactional
    public Page<Movie> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    // 제목 키워드로 페이징 검색
    @Transactional
    public Page<Movie> searchMoviesByTitle(String keyword, Pageable pageable) {
        return movieRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    // 단일 영화 조회
    @Transactional
    public Movie getMovie(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found: " + id));
    }

    @Transactional
    public Page<Movie> searchOrFetchMoviesByTitle(String keyword, Pageable pageable) {
        // 우선 DB에서 제목 검색
        Page<Movie> page = movieRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        if (page.hasContent()) {
            log.info("DB에서 '{}' 검색 결과 발견 (건수: {})", keyword, page.getTotalElements());
            return page;
        } else {
            log.info("DB에서 '{}' 검색 결과 없음. TMDB API 호출 시도.", keyword);
        }

        // TMDB Search API 호출 (한 페이지만)
        int tmdbPage = pageable.getPageNumber() + 1;
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("language", language)
                .queryParam("query", keyword)
                .queryParam("page", tmdbPage)
                .build()
                .toUriString();

        ResponseEntity<TmdbResponse> resp = restTemplate.getForEntity(url, TmdbResponse.class);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            List<Movie> toSave = resp.getBody().getResults().stream()
                // releaseDate가 없으면 저장하지 않음
                .filter(dto -> dto.getReleaseDate() != null)
                // 이미 DB에 있으면 저장하지 않음
                .filter(dto -> !movieRepository.existsByTmdbMovieId(dto.getId()))
                .map(this::toEntity)
                .collect(Collectors.toList());

            if (!toSave.isEmpty()) {
                movieRepository.saveAll(toSave);
                log.info("TMDB에서 '{}' 검색결과 저장 완료 (건수: {})", keyword, toSave.size());
            } else {
                log.info("TMDB에서 '{}' 검색했지만 저장할 신규 데이터가 없음.", keyword);
            }
        } else {
            log.warn("TMDB API 호출 실패 또는 응답 본문 없음 (상태 코드: {})",
            resp != null ? resp.getStatusCode() : "null response");
    }

        // 다시 DB에서 페이징 조회
        return movieRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

}
