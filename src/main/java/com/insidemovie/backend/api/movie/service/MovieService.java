package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.constant.MovieLanguage;
import com.insidemovie.backend.api.movie.dto.GenreDto;
import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionSummaryResponseDTO;
import com.insidemovie.backend.api.movie.dto.tmdb.*;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final MovieEmotionSummaryRepository movieEmotionSummaryRepository;

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

    /**
     * TMDB에서 지정한 타입(type)의 영화 목록을 페이지 단위로 조회해
     * 각 영화의 상세정보(fetchAndSaveMovieById)로 저장합니다.
     */
    @Transactional
    public void fetchAndSaveMoviesByPage(String type, int page, boolean isInitial) {
        String url = String.format(
            "%s/movie/%s?api_key=%s&language=%s&page=%d",
            baseUrl, type, apiKey, language, page
        );

        ResponseEntity<SearchMovieWrapperDTO> response =
            restTemplate.getForEntity(url, SearchMovieWrapperDTO.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return;
        }

        for (SearchMovieResponseDTO dto : response.getBody().getResults()) {
            // 지원 언어 필터링
            if (!MovieLanguage.isAllowed(dto.getOriginalLanguage())) {
                log.info("[필터링] 지원하지 않는 언어({}) 영화(ID={}) 건너뜀",
                    dto.getOriginalLanguage(), dto.getId());
                continue;
            }
            // 성인 영화 스킵
            if (Boolean.TRUE.equals(dto.getAdult())) {
                log.info("성인 영화 스킵: ID={} / {}", dto.getId(), dto.getTitle());
                continue;
            }
            // 상세 저장
            fetchAndSaveMovieById(dto.getId());
        }
    }

    /**
     * TMDB에서 단일 영화 ID로 상세정보를 가져와 DB에 저장합니다.
     */
    @Transactional
    public void fetchAndSaveMovieById(Long tmdbId) {
        // 1) 상세정보 호출 (credits, release_dates, watch/providers 포함)
        String detailUrl = String.format(
            "%s/movie/%d?api_key=%s&language=%s&append_to_response=credits,release_dates,watch/providers",
            baseUrl, tmdbId, apiKey, language
        );
        ResponseEntity<MovieDetailDTO> detailRes =
            restTemplate.getForEntity(detailUrl, MovieDetailDTO.class);
        if (!detailRes.getStatusCode().is2xxSuccessful() || detailRes.getBody() == null) {
            log.warn("TMDB 상세정보 조회 실패: ID={}", tmdbId);
            return;
        }
        MovieDetailDTO detail = detailRes.getBody();

        // 2) DB에서 조회 또는 신규 생성
        Movie movie = movieRepository.findByTmdbMovieId(tmdbId)
            .orElseGet(() -> Movie.builder()
                .tmdbMovieId(tmdbId)
                .build()
            );

        // 3) 헬퍼로 매핑 & 저장
        applyDetailToMovie(movie, detail);
        movieRepository.save(movie);

        log.info("[TMDB 연동] 저장 완료: TMDB ID={}", tmdbId);
    }

    /**
     * MovieDetailDTO의 모든 필드를 Movie 엔티티에 매핑하는 공통 헬퍼 메서드
     */
    private void applyDetailToMovie(Movie movie, MovieDetailDTO detail) {
        // 포스터/배경 전체 URL
        String fullPoster   = imageBaseUrl + posterSize + detail.getPosterPath();
        String fullBackdrop = imageBaseUrl + posterSize + detail.getBackdropPath();

        // 평점 반올림
        double avg = detail.getVoteAverage() == null ? 0 : detail.getVoteAverage();
        double rounded = BigDecimal.valueOf(avg)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();

        movie.updateTitle(detail.getTitle());
        movie.updateOverview(detail.getOverview());
        movie.updatePosterPath(fullPoster);
        movie.updateBackDropPath(fullBackdrop);
        movie.updateVoteAverage(rounded);
        movie.setVoteCount(detail.getVoteCount());
        movie.updateOriginalLanguage(detail.getOriginalLanguage());
        movie.updateReleaseDate(detail.getReleaseDate());
        movie.updatePopularity(detail.getPopularity());

        // 장르 ID 리스트
        List<Long> genreIds = detail.getGenres().stream()
            .map(GenreDto::getId)
            .collect(Collectors.toList());
        movie.updateGenreIds(genreIds);

        movie.setTitleEn(detail.getOriginalTitle());

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

        movie.setRuntime(detail.getRuntime());
        movie.setStatus(detail.getStatus());

        // KR 등급
        String rating = detail.getReleaseDates().getResults().stream()
            .filter(r -> "KR".equals(r.getIso3166_1()))
            .flatMap(r -> r.getReleaseDates().stream())
            .map(ReleaseDateDTO::getCertification)
            .filter(cert -> cert != null && !cert.isEmpty())
            .findFirst().orElse(null);
        movie.setRating(rating);

        // KR OTT 제공처
        List<String> ottProviders = Optional.ofNullable(detail.getWatchProviders()
                .getResults().get("KR"))
            .map(cp -> Optional.ofNullable(cp.getFlatrate()).orElse(Collections.emptyList())
                .stream()
                .map(ProviderDTO::getProviderName)
                .collect(Collectors.toList()))
            .orElse(Collections.emptyList());
        movie.setOttProviders(ottProviders.toString());
    }

    /**
     * 제목 + 개봉연도로 TMDB에서 영화 검색 후 첫 번째 결과 반환
     */
    @Transactional
    public Optional<SearchMovieResponseDTO> searchMovieByTitleAndYear(String title, int year) {
        String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String url = String.format(
            "%s/search/movie?api_key=%s&language=%s&query=%s&primary_release_year=%d",
            baseUrl, apiKey, language, encoded, year
        );
        ResponseEntity<SearchMovieWrapperDTO> resp =
            restTemplate.getForEntity(url, SearchMovieWrapperDTO.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            return Optional.empty();
        }
        return resp.getBody().getResults().stream().findFirst();
    }

    /**
     * 필요에 따라 Movie ↔ DTO 비교 로직을 유지할 수도 있습니다.
     */
    private boolean hasChanged(Movie movie, SearchMovieResponseDTO dto) {
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

    /**
     * 영화에 저장된 5가지 감정 상태를 조회해 DTO로 반환
     */
    @Transactional
    public MovieEmotionSummaryResponseDTO getMovieEmotions(Long movieId) {
        return movieEmotionSummaryRepository.findByMovieId(movieId)
            .map(summary -> {
                MovieEmotionSummaryResponseDTO dto = new MovieEmotionSummaryResponseDTO();
                dto.setJoy(summary.getJoy());
                dto.setSadness(summary.getSadness());
                dto.setFear(summary.getFear());
                dto.setAnger(summary.getAnger());
                dto.setNeutral(summary.getNeutral());
                dto.setDominantEmotion(summary.getDominantEmotion().name());
                return dto;
            })
            .orElseGet(() -> {
                // 데이터가 없을 때 빈 DTO 반환
                MovieEmotionSummaryResponseDTO dto = new MovieEmotionSummaryResponseDTO();
                dto.setJoy(0f);
                dto.setSadness(0f);
                dto.setFear(0f);
                dto.setAnger(0f);
                dto.setNeutral(0f);
                dto.setDominantEmotion("NONE");
                return dto;
            });
    }
}
