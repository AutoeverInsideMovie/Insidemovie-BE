package com.insidemovie.backend.api.movie.service;
import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.constant.MovieLanguage;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;

import com.insidemovie.backend.api.movie.dto.TmdbGenreResponseDto;
import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionSummaryResponseDTO;
import com.insidemovie.backend.api.movie.dto.tmdb.*;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieGenre;
import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;

import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;

import com.insidemovie.backend.api.review.repository.EmotionRepository;

import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;
    private final MovieGenreRepository movieGenreRepository;
    private final EmotionRepository emotionRepository;
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

        movieGenreRepository.deleteByMovie(movie);
        //새 매핑 생성: DTO→enum→MovieGenre
        detail.getGenres().stream()
                .map(TmdbGenreResponseDto::getId)                  // List<Long>
                .map(Long::intValue)                   // int
                .map(id -> GenreType.fromId(id)        // TMDB ID → GenreType enum
                        .orElseThrow(() ->
                                new NotFoundException("Unknown Genre ID: " + id)))
                .forEach(gt -> {
                    MovieGenre mg = MovieGenre.builder()
                            .movie(movie)
                            .genreType(gt)                  // @Enumerated(EnumType.STRING) 필드
                            .build();
                    movieGenreRepository.save(mg);
                });

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
//        List<Long> genreIds = detail.getGenres().stream()
//            .map(GenreDto::getId)
//            .collect(Collectors.toList());
//        movie.updateGenreIds(genreIds);

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
            //|| !Objects.equals(movie.getGenreIds(), dto.getGenreIds())
            || !Objects.equals(movie.getOriginalLanguage(), dto.getOriginalLanguage())
            || !Objects.equals(movie.getReleaseDate(),
                dto.getReleaseDate() != null ? dto.getReleaseDate() : null);
    }


    public PageResDto<MovieSearchResDto> movieSearchTitle(String title, Integer page, Integer pageSize){
        int zeroBasedPage = (page != null && page > 0) ? page - 1 : 0;
        Pageable pageable = PageRequest.of(zeroBasedPage, pageSize);

        Page<Movie> movies = movieRepository.findByTitleContainingIgnoreCase(title, pageable);
        if(movies.isEmpty()){
            throw new NotFoundException("제목이 '" + title + "'인 영화를 찾을 수 없습니다.");
        }
        Page<MovieSearchResDto> movieSearchResDtos = movies.map(this::convertEntityToDto);
        return new PageResDto<>(movieSearchResDtos);
    }
    /*
     * TODO: 영화 장르와, 타이틀로 검색했을때 검색되도록
     *   - "액"이 포함된 영화 타이틀을 검색하고 싶어도 액션으로 인식되어 액션 영화 나옴
     *   - 수정 방안 생각중
     */
    public PageResDto<MovieSearchResDto> searchByQuery(String q, Integer page, Integer pageSize) {
        int zeroBasedPage = (page != null && page > 0) ? page - 1 : 0;
        Pageable pageable = PageRequest.of(zeroBasedPage, pageSize);
        // 1) q로 매칭되는 GenreType 리스트
        List<GenreType> matched = Arrays.stream(GenreType.values())
                .filter(gt -> gt.name().contains(q))
                .toList();

        Page<Movie> moviePage;
        if (!matched.isEmpty()) {
            // 장르 검색: MovieGenre 페이지 조회 → Movie 페이지로 변환
            Page<MovieGenre> mgPage = movieGenreRepository.findByGenreTypeIn(matched, pageable);
            moviePage = mgPage.map(MovieGenre::getMovie);
        } else {
            // 제목 검색
            moviePage = movieRepository.findByTitleContainingIgnoreCase(q, pageable);
        }
        Page<MovieSearchResDto> dto = moviePage.map(this::convertEntityToDto);
        return new PageResDto<>(dto);
    }

    private MovieSearchResDto convertEntityToDto(Movie movie) {
        MovieSearchResDto movieSearchResDto = new MovieSearchResDto();
        movieSearchResDto.setId(movie.getId());
        movieSearchResDto.setTitle(movie.getTitle());
        movieSearchResDto.setPosterPath(movie.getPosterPath());
        movieSearchResDto.setVoteAverage(movie.getVoteAverage());
        return movieSearchResDto;
    }
    // 영화에 달린 리뷰들의 감정 평균 조회
    @Transactional
    public EmotionAvgDTO getMovieEmotionSummary(Long movieId) {

        // 감정 평균 조회
        EmotionAvgDTO avg = emotionRepository.findAverageEmotionsByMovieId(movieId)
                .orElseGet(() -> EmotionAvgDTO.builder()
                        .joy(0.0).sadness(0.0).anger(0.0).fear(0.0).neutral(0.0)
                        .repEmotionType(EmotionType.NEUTRAL)
                        .build());

        // 대표 감정 계산
        EmotionType rep = calculateRepEmotion(avg);
        avg.setRepEmotionType(rep);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));
        // 요약 엔티티 조회 or 생성
        MovieEmotionSummary summary = movieEmotionSummaryRepository
                .findByMovieId(movieId)
                .orElseGet(() -> MovieEmotionSummary.builder()
                        .movie(movie)
                        .build());

        // 엔티티 업데이트 및 저장
        summary.updateFromDTO(avg);
        movieEmotionSummaryRepository.save(summary);

        return avg;
    }

    // 대표 감정 계산 메서드
    private EmotionType calculateRepEmotion(EmotionAvgDTO dto) {
        Map<EmotionType, Double> scores = Map.of(
                EmotionType.JOY, dto.getJoy(),
                EmotionType.SADNESS, dto.getSadness(),
                EmotionType.ANGER, dto.getAnger(),
                EmotionType.FEAR, dto.getFear(),
                EmotionType.NEUTRAL, dto.getNeutral()
        );

        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(EmotionType.NEUTRAL);
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

    /**
     * DB에 저장된 영화를 popularity 내림차순으로 페이징 조회하여
     * SearchMovieWrapperDTO 형태로 반환
     */
    public SearchMovieWrapperDTO getPopularMovies(int page, int pageSize) {
        int zeroBasedPage = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(
            zeroBasedPage,
            pageSize,
            Sort.by(Sort.Direction.DESC, "popularity")
        );

        Page<Movie> moviePage = movieRepository.findAllByOrderByPopularityDesc(pageable);

        List<SearchMovieResponseDTO> results = moviePage.stream()
            .map(this::convertEntityToSearchMovieResponseDTO)
            .collect(Collectors.toList());

        SearchMovieWrapperDTO wrapper = new SearchMovieWrapperDTO();
        wrapper.setPage(page);
        wrapper.setResults(results);
        wrapper.setTotalPages(moviePage.getTotalPages());
        wrapper.setTotalResults((int) moviePage.getTotalElements());
        return wrapper;
    }

    /**
     * Movie 엔티티를 TMDB SearchMovieResponseDTO 형태로 매핑
     */
    private SearchMovieResponseDTO convertEntityToSearchMovieResponseDTO(Movie movie) {
        SearchMovieResponseDTO dto = new SearchMovieResponseDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setOverview(movie.getOverview());
        dto.setPosterPath(movie.getPosterPath());
        dto.setBackDropPath(movie.getBackdropPath());
        dto.setVoteAverage(movie.getVoteAverage());
        dto.setVoteCount(movie.getVoteCount());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setOriginalLanguage(movie.getOriginalLanguage());
        dto.setPopularity(movie.getPopularity());
        // DB에는 adult 정보가 없으므로 기본값 false 설정
        dto.setAdult(false);
        return dto;
    }
}
