package com.insidemovie.backend.api.movie.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.constant.MovieLanguage;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.movie.dto.TmdbGenreResponseDto;
import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionResDTO;
import com.insidemovie.backend.api.movie.dto.tmdb.*;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import com.insidemovie.backend.api.movie.entity.MovieGenre;
import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.entity.Review;
import com.insidemovie.backend.api.review.repository.EmotionRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
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

    private final ObjectMapper objectMapper;
    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;
    private final MovieGenreRepository movieGenreRepository;
    private final EmotionRepository emotionRepository;
    private final MovieEmotionSummaryRepository movieEmotionSummaryRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

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
    public boolean fetchAndSaveMoviesByPage(String type, int page, boolean isInitial) {
        String url = String.format(
            "%s/movie/%s?api_key=%s&language=%s&page=%d",
            baseUrl, type, apiKey, language, page
        );

        ResponseEntity<SearchMovieWrapperDTO> response =
            restTemplate.getForEntity(url, SearchMovieWrapperDTO.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.warn("[페이지 실패] type={} page={} status={}", type, page, response.getStatusCode());
            return false;
        }

        SearchMovieWrapperDTO body = response.getBody();
        List<SearchMovieResponseDTO> results = body.getResults();
        if (results == null || results.isEmpty()) {
            log.info("[빈 페이지] type={} page={}", type, page);
            return false; // 조기 종료 근거
        }

        for (SearchMovieResponseDTO dto : results) {
            if (!MovieLanguage.isAllowed(dto.getOriginalLanguage())) {
                log.debug("[필터링] 언어({}) skip id={}", dto.getOriginalLanguage(), dto.getId());
                continue;
            }
            if (Boolean.TRUE.equals(dto.getAdult())) {
                log.debug("[필터링] 성인 skip id={}", dto.getId());
                continue;
            }
            fetchAndSaveMovieById(dto.getId());
        }
        return true;
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

        movieEmotionSummaryRepository.findByMovieId(movie.getId())
                .orElseGet(()->{
                    EmotionAvgDTO initialEmotion = EmotionAvgDTO.builder()
                            .joy(0.0).sadness(0.0).anger(0.0).fear(0.0).disgust(0.0)
                            .repEmotionType(EmotionType.NONE)
                            .build();
                    MovieEmotionSummary newSummary = MovieEmotionSummary.builder()
                            .movie(movie)
                            .build();
                    newSummary.updateFromDTO(initialEmotion);
                    return movieEmotionSummaryRepository.save(newSummary);

                });

        log.info("[TMDB 연동] 저장 완료: TMDB ID={}", tmdbId);
    }

    /**
     * MovieDetailDTO의 모든 필드를 Movie 엔티티에 매핑하는 공통 헬퍼 메서드
     */
    private void applyDetailToMovie(Movie movie, MovieDetailDTO detail) {
        String fullPoster   = imageBaseUrl + posterSize + detail.getPosterPath();
        String fullBackdrop = imageBaseUrl + posterSize + detail.getBackdropPath();

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

        // 배우 리스트
        List<String> actors = detail.getCredits().getCast().stream()
                .map(CastDTO::getName)
                .toList();
        movie.setActors(writeJsonArray(actors));   // ["A","B","C"]

        // 감독 리스트
        List<String> directors = detail.getCredits().getCrew().stream()
                .filter(c -> "Director".equals(c.getJob()))
                .map(CrewDTO::getName)
                .distinct()
                .toList();
        movie.setDirectors(writeJsonArray(directors));

        movie.setRuntime(detail.getRuntime());
        movie.setStatus(detail.getStatus());

        // 한국 등급
        String rating = detail.getReleaseDates().getResults().stream()
                .filter(r -> "KR".equals(r.getIso3166_1()))
                .flatMap(r -> r.getReleaseDates().stream())
                .map(ReleaseDateDTO::getCertification)
                .filter(cert -> cert != null && !cert.isEmpty())
                .findFirst()
                .orElse(null);
        movie.setRating(rating);

        // 한국 OTT 제공
        List<String> ottProviders = Optional.ofNullable(detail.getWatchProviders().getResults().get("KR"))
                .map(cp -> Optional.ofNullable(cp.getFlatrate()).orElse(Collections.emptyList())
                        .stream()
                        .map(ProviderDTO::getProviderName)
                        .toList())
                .orElse(Collections.emptyList());
        movie.setOttProviders(writeJsonArray(ottProviders));
    }

    private String writeJsonArray(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list); // 예: ["배우1","배우2"]
        } catch (Exception e) {
            log.warn("JSON 직렬화 실패. list={}", list, e);
            return "[]";
        }
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
        Pageable pageable = PageRequest.of(page, pageSize);

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
        Pageable pageable = PageRequest.of(page, pageSize);
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
        // 영화 대표 감정 가져오기
        EmotionAvgDTO avg = getMovieEmotionSummary(movie.getId());
        EmotionType mainEmotion = avg.getRepEmotionType();

        // 감정 수치 계산
        double mainEmotionValue = switch (mainEmotion) {
            case JOY -> avg.getJoy();
            case SADNESS -> avg.getSadness();
            case ANGER -> avg.getAnger();
            case FEAR -> avg.getFear();
            case DISGUST -> avg.getDisgust();
            case NONE -> 0.0;
        };

        Double ratingAvg = reviewRepository.findAverageByMovieId(movie.getId());
        BigDecimal rounded;
        if(ratingAvg==null || ratingAvg==0.00){
            rounded=BigDecimal.ZERO.setScale(2);
        }else{
            rounded= BigDecimal.valueOf(ratingAvg)
            .setScale(2, RoundingMode.HALF_UP);
        }


        MovieSearchResDto movieSearchResDto = new MovieSearchResDto();
        movieSearchResDto.setId(movie.getId());
        movieSearchResDto.setTitle(movie.getTitle());
        movieSearchResDto.setPosterPath(movie.getPosterPath());
        movieSearchResDto.setVoteAverage(movie.getVoteAverage());
        movieSearchResDto.setMainEmotion(mainEmotion);
        movieSearchResDto.setMainEmotionValue(mainEmotionValue);
        movieSearchResDto.setRatingAvg(rounded);
        return movieSearchResDto;
    }

    // 영화에 달린 리뷰들의 감정 평균 조회
    @Transactional
    public EmotionAvgDTO getMovieEmotionSummary(Long movieId) {

        // 감정 평균 조회
        EmotionAvgDTO avg = emotionRepository.findAverageEmotionsByMovieId(movieId)
                .orElseGet(() -> EmotionAvgDTO.builder()
                        .joy(0.0).sadness(0.0).anger(0.0).fear(0.0).disgust(0.0)
                        .repEmotionType(EmotionType.DISGUST)
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
    EmotionType calculateRepEmotion(EmotionAvgDTO dto) {

        // 모든 값이 0인 경우 NONE 고정
        if (dto.getJoy() == 0.0 &&
                dto.getSadness() == 0.0 &&
                dto.getAnger() == 0.0 &&
                dto.getFear() == 0.0 &&
                dto.getDisgust() == 0.0) {
            return EmotionType.NONE;
        }

        return Map.<EmotionType, Double>of(
                        EmotionType.JOY,     dto.getJoy(),
                        EmotionType.SADNESS, dto.getSadness(),
                        EmotionType.ANGER,   dto.getAnger(),
                        EmotionType.FEAR,    dto.getFear(),
                        EmotionType.DISGUST, dto.getDisgust()
                ).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();
    }

    /**
     * 영화에 저장된 5가지 감정 상태를 조회해 DTO로 반환
     */
    @Transactional
    public MovieEmotionResDTO getMovieEmotions(Long movieId) {
        return movieEmotionSummaryRepository.findByMovieId(movieId)
            .map(summary -> {
                MovieEmotionResDTO dto = new MovieEmotionResDTO();
                dto.setJoy(summary.getJoy());
                dto.setSadness(summary.getSadness());
                dto.setFear(summary.getFear());
                dto.setAnger(summary.getAnger());
                dto.setDisgust(summary.getDisgust());
                dto.setDominantEmotion(EmotionType.valueOf(summary.getDominantEmotion().name()));
                return dto;
            })
            .orElseGet(() -> {
                // 데이터가 없을 때 빈 DTO 반환
                MovieEmotionResDTO dto = new MovieEmotionResDTO();
                dto.setJoy(0f);
                dto.setSadness(0f);
                dto.setFear(0f);
                dto.setAnger(0f);
                dto.setDisgust(0f);
                dto.setDominantEmotion(EmotionType.valueOf("NONE"));
                return dto;
            });
    }

    /**
     * DB에 저장된 영화를 popularity 내림차순으로 페이징 조회하여
     * SearchMovieWrapperDTO 형태로 반환
     */
    public SearchMovieWrapperDTO getPopularMovies(int page, int pageSize) {

        Pageable pageable = PageRequest.of(
            page,
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

    public PageResDto<MovieSearchResDto> getRecommendedMoviesByLatest(GenreType genreType, Integer page, Integer pageSize){
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Movie> moviePage = movieRepository.findMoviesByGenreTypeOrderByReleaseDateDesc(genreType, pageable);
        if (moviePage.isEmpty()) {
            throw new NotFoundException("해당 장르의 영화가 없습니다: " + genreType.name());
        }


        return new PageResDto<MovieSearchResDto> (moviePage.map(movie -> {
            MovieSearchResDto dto = new MovieSearchResDto();
            EmotionAvgDTO avg = getMovieEmotionSummary(movie.getId());
            EmotionType mainEmotion = avg.getRepEmotionType();

            double mainEmotionValue = switch (mainEmotion) {
                case JOY -> avg.getJoy();
                case SADNESS -> avg.getSadness();
                case ANGER -> avg.getAnger();
                case FEAR -> avg.getFear();
                case DISGUST -> avg.getDisgust();
                case NONE -> 0.0;
            };
            Double ratingAvg = reviewRepository.findAverageByMovieId(movie.getId());
            BigDecimal rounded;
            if(ratingAvg==null || ratingAvg==0.00){
                rounded=BigDecimal.ZERO.setScale(2);
            }else{
                rounded= BigDecimal.valueOf(ratingAvg)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            dto.setId(movie.getId());
            dto.setTitle(movie.getTitle());
            dto.setPosterPath(movie.getPosterPath());
            dto.setVoteAverage(movie.getVoteAverage());
            dto.setReleaseDate(movie.getReleaseDate());
            dto.setMainEmotion(mainEmotion);
            dto.setMainEmotionValue(mainEmotionValue);
            dto.setRatingAvg(rounded);
            return dto;
        }));
    }

    public PageResDto<MovieSearchResDto> getRecommendedMoviesByPopularity(GenreType genreType, Integer page, Integer pageSize){
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Movie> moviePage = movieRepository.findMoviesByGenreTypeOrderByVoteAverageDesc(genreType, pageable);
        if (moviePage.isEmpty()) {
            throw new NotFoundException("해당 장르의 영화가 없습니다: " + genreType.name());
        }

        Page<MovieSearchResDto> dto = moviePage.map(movie -> {
            MovieSearchResDto resDto = new MovieSearchResDto();
            EmotionAvgDTO avg = getMovieEmotionSummary(movie.getId());
            EmotionType mainEmotion = avg.getRepEmotionType();

            double mainEmotionValue = switch (mainEmotion) {
                case JOY -> avg.getJoy();
                case SADNESS -> avg.getSadness();
                case ANGER -> avg.getAnger();
                case FEAR -> avg.getFear();
                case DISGUST -> avg.getDisgust();
                case NONE -> 0.0;
            };
            Double ratingAvg = reviewRepository.findAverageByMovieId(movie.getId());
            BigDecimal rounded;
            if(ratingAvg==null || ratingAvg==0.00){
                rounded=BigDecimal.ZERO.setScale(2);
            }else{
                rounded= BigDecimal.valueOf(ratingAvg)
                        .setScale(2, RoundingMode.HALF_UP);
            }
            resDto.setId(movie.getId());
            resDto.setTitle(movie.getTitle());
            resDto.setPosterPath(movie.getPosterPath());
            resDto.setVoteAverage(movie.getVoteAverage());
            resDto.setReleaseDate(movie.getReleaseDate());
            resDto.setMainEmotion(mainEmotion);
            resDto.setMainEmotionValue(mainEmotionValue);
            resDto.setRatingAvg(rounded);
            return resDto;
        });

        PageResDto<MovieSearchResDto> result = new PageResDto<>(dto);
        return result;
    }

    public PageResDto<MovieSearchResDto> getMyWatchedMovies(String memberEmail, Integer page, Integer pageSize){
        Pageable pageable = PageRequest.of(page, pageSize);

        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
        Page<Review> moviePage = reviewRepository.findByMember(member, pageable);

        Page<MovieSearchResDto> dto = moviePage.map(movielike ->{
            Movie movie = movielike.getMovie();
            EmotionAvgDTO avg = getMovieEmotionSummary(movie.getId());

            Double ratingAvg = reviewRepository.findAverageByMovieId(movie.getId());
            BigDecimal rounded;
            if(ratingAvg==null || ratingAvg==0.00){
                rounded=BigDecimal.ZERO.setScale(2);
            }else{
                rounded= BigDecimal.valueOf(ratingAvg)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            EmotionType mainEmotion = avg.getRepEmotionType();
            Double mainEmotionValue = switch (mainEmotion) {
                case JOY     -> avg.getJoy();
                case SADNESS -> avg.getSadness();
                case ANGER   -> avg.getAnger();
                case FEAR    -> avg.getFear();
                case DISGUST -> avg.getDisgust();
                default      -> 0.0;
            };

            return MovieSearchResDto.builder()
                    .id(movie.getId())
                    .posterPath(movie.getPosterPath())
                    .title(movie.getTitle())
                    .voteAverage(movie.getVoteAverage())
                    .mainEmotion(mainEmotion)
                    .mainEmotionValue(mainEmotionValue)
                    .ratingAvg(rounded)
                    .build();
        });
        return new PageResDto<>(dto);

    }
    @Transactional
    public int fetchTotalPages(String type) {
        String url = String.format("%s/movie/%s?api_key=%s&language=%s&page=1",
                baseUrl, type, apiKey, language);

        ResponseEntity<SearchMovieWrapperDTO> response =
                restTemplate.getForEntity(url, SearchMovieWrapperDTO.class);

        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null) {
            log.warn("[totalPages] 응답 실패 type={}", type);
            return 0;
        }

        int totalPages = response.getBody().getTotalPages();
        if (totalPages <= 0) {
            log.warn("[totalPages] 비정상 totalPages={} type={}", totalPages, type);
            return 0;
        }
        if (totalPages > 499) {
            totalPages = 499;
        }
        return totalPages;
    }
}
