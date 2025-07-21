package com.insidemovie.backend.api.movie.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeListDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeRequestDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.DailyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.WeeklyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.tmdb.SearchMovieResponseDTO;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.boxoffice.DailyBoxOfficeEntity;
import com.insidemovie.backend.api.movie.entity.boxoffice.WeeklyBoxOfficeEntity;
import com.insidemovie.backend.api.movie.repository.DailyBoxOfficeRepository;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.movie.repository.WeeklyBoxOfficeRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.BaseException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoxOfficeService {

    @Value("${kobis.api.key}")
    private String kobisApiKey;

    private final ObjectMapper objectMapper;
    private final MovieService movieService;
    private final DailyBoxOfficeRepository dailyRepo;
    private final WeeklyBoxOfficeRepository weeklyRepo;
    private final MovieRepository movieRepo;
    private final MovieGenreRepository movieGenreRepository;
    private final ReviewRepository reviewRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String DAILY_URL_JSON =
        "http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json";
    private static final String WEEKLY_URL_JSON =
        "http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchWeeklyBoxOfficeList.json";

    // 일간 박스오피스 조회 및 저장
    @Transactional
    public void fetchAndStoreDailyBoxOffice(BoxOfficeRequestDTO req) {
        LocalDate date = LocalDate.now().minusDays(1);
        log.info("[Service] WILL UPSERT daily date = {}", date);
        int limit = req.getItemPerPage();

        log.info("[Daily] Fetch & Upsert for date={} (limit={})", date, limit);

        List<DailyBoxOfficeEntity> fetched = fetchDailyFromApi(date, limit);

        for (DailyBoxOfficeEntity incoming : fetched) {
            // (movieCd, targetDate) 로 기존 검색
            DailyBoxOfficeEntity entity = dailyRepo
                    .findByTargetDateAndMovieCd(date, incoming.getMovieCd())
                    .map(existing -> { existing.updateFrom(incoming); return existing; })
                    .orElse(incoming); // 새 엔티티

            // TMDB 연동 (없을 때만 저장)
            movieService.searchMovieByTitleAndYear(
                    entity.getMovieName(),
                    parseYearSafe(entity.getOpenDate())
            ).ifPresent(dto -> {
                // Movie 없으면 저장
                movieRepo.findByTmdbMovieId(dto.getId())
                        .or(() -> {
                            movieService.fetchAndSaveMovieById(dto.getId());
                            return movieRepo.findByTmdbMovieId(dto.getId());
                        })
                        .ifPresent(entity::setMovie);
            });

            dailyRepo.save(entity);
        }

        log.info("[Daily] Upsert completed (count={}) for {}", fetched.size(), date);
    }

    private int parseYearSafe(String openDate) {
        try {
            if (openDate == null || openDate.isBlank()) {
                return LocalDate.now().getYear();
            }
            return LocalDate.parse(openDate, ISO_FMT).getYear();
        } catch (Exception e) {
            return LocalDate.now().getYear();
        }
    }

    // 외부 API 호출하여 일간 엔티티 목록 생성
    private List<DailyBoxOfficeEntity> fetchDailyFromApi
    (
        LocalDate date,
        int itemPerPage
    ) {
        String targetDt = date.format(FMT);
        RestTemplate rest = new RestTemplate();
        String uri = UriComponentsBuilder.fromHttpUrl(DAILY_URL_JSON)
            .queryParam("key", kobisApiKey)
            .queryParam("targetDt", targetDt)
            .queryParam("itemPerPage", itemPerPage)
            .toUriString();

        JsonNode listNode = rest.getForObject(uri, JsonNode.class)
            .path("boxOfficeResult")
            .path("dailyBoxOfficeList");

        return StreamSupport.stream(listNode.spliterator(), false)
            .limit(itemPerPage)
            .map(node -> DailyBoxOfficeEntity.builder()
                .targetDate(date)
                .rnum(node.path("rnum").asText())
                .movieRank(node.path("rank").asText())
                .rankInten(node.path("rankInten").asText())
                .rankOldAndNew(node.path("rankOldAndNew").asText())
                .movieCd(node.path("movieCd").asText())
                .movieName(node.path("movieNm").asText())
                .openDate(node.path("openDt").asText())
                .salesShare(node.path("salesShare").asText())
                .salesInten(node.path("salesInten").asText())
                .salesChange(node.path("salesChange").asText())
                .salesAcc(node.path("salesAcc").asText())
                .audiCnt(node.path("audiCnt").asText())
                .audiInten(node.path("audiInten").asText())
                .audiChange(node.path("audiChange").asText())
                .audiAcc(node.path("audiAcc").asText())
                .scrnCnt(node.path("scrnCnt").asText())
                .showCnt(node.path("showCnt").asText())
                .build())
            .collect(Collectors.toList());
    }

    // 주간 박스오피스 조회 및 저장
    @Transactional
    public void fetchAndStoreWeeklyBoxOffice(BoxOfficeRequestDTO req) {
        // 1) 지난주 기준 날짜 & 연주차 계산
        LocalDate lastWeekDate = LocalDate.now().minusWeeks(1);
        String targetDt = lastWeekDate.format(FMT);
        WeekFields wf = WeekFields.ISO;
        int weekOfYear = lastWeekDate.get(wf.weekOfWeekBasedYear());
        int year = lastWeekDate.get(wf.weekBasedYear());
        String yearWeek = String.format("%04dIW%02d", year, weekOfYear);

        log.info("[Service] WeeklyBoxOffice for last week {} (yearWeek={})", targetDt, yearWeek);

        // 2) KOFIC API 호출 (단 한 번)
        List<WeeklyBoxOfficeEntity> fetched =
            fetchWeeklyFromApi(lastWeekDate, req.getWeekGb(), req.getItemPerPage(), yearWeek);

        // 3) 업서트 루프
        for (WeeklyBoxOfficeEntity e : fetched) {
            // 3-1) 기존 데이터 조회
            Optional<WeeklyBoxOfficeEntity> opt =
                weeklyRepo.findByYearWeekTimeAndMovieCd(yearWeek, e.getMovieCd());

            WeeklyBoxOfficeEntity toSave = opt
                .map(existing -> {
                    existing.updateFrom(e);
                    return existing;
                })
                .orElse(e);

            // 4) TMDB 연동: movieId가 없으면 한 번만 저장
            movieService.searchMovieByTitleAndYear(
                    toSave.getMovieNm(),
                    LocalDate.parse(toSave.getOpenDt(), ISO_FMT).getYear()
                )
                .ifPresent(dto -> {
                    Optional<Movie> existingMovie = movieRepo.findByTmdbMovieId(dto.getId());
                    if (existingMovie.isEmpty()) {
                        movieService.fetchAndSaveMovieById(dto.getId());
                    }
                    log.info("[연동완료] {} ({}) → TMDB ID={}",
                        toSave.getMovieNm(), toSave.getMovieCd(), dto.getId());
                });

            // 5) 저장 (insert 또는 update)
            weeklyRepo.save(toSave);
        }

        log.info("[Service] Completed upsert of {} weekly box office records", fetched.size());
    }

    // 외부 API 호출하여 주간 엔티티 목록 생성
    private List<WeeklyBoxOfficeEntity> fetchWeeklyFromApi(
        LocalDate date,
        String weekGb,
        int itemPerPage,
        String yearWeek
    ) {
        String targetDt = date.format(FMT);
        String uri = UriComponentsBuilder
            .fromHttpUrl(WEEKLY_URL_JSON)
            .queryParam("key", kobisApiKey)
            .queryParam("targetDt", targetDt)
            .queryParam("weekGb", weekGb)
            .queryParam("itemPerPage", itemPerPage)
            .toUriString();

        JsonNode listNode = new RestTemplate().getForObject(uri, JsonNode.class)
            .path("boxOfficeResult")
            .path("weeklyBoxOfficeList");

        return StreamSupport.stream(listNode.spliterator(), false)
            .limit(itemPerPage)
            .map(node -> WeeklyBoxOfficeEntity.builder()
                .yearWeekTime(yearWeek)
                .rnum(node.path("rnum").asText())
                .movieRank(node.path("rank").asText())
                .rankInten(node.path("rankInten").asText())
                .rankOldAndNew(node.path("rankOldAndNew").asText())
                .movieCd(node.path("movieCd").asText())
                .movieNm(node.path("movieNm").asText())
                .openDt(node.path("openDt").asText())
                .salesAmt(node.path("salesAmt").asText())
                .salesShare(node.path("salesShare").asText())
                .salesInten(node.path("salesInten").asText())
                .salesChange(node.path("salesChange").asText())
                .salesAcc(node.path("salesAcc").asText())
                .audiCnt(node.path("audiCnt").asText())
                .audiInten(node.path("audiInten").asText())
                .audiChange(node.path("audiChange").asText())
                .audiAcc(node.path("audiAcc").asText())
                .scrnCnt(node.path("scrnCnt").asText())
                .showCnt(node.path("showCnt").asText())
                .build()
            )
            .collect(Collectors.toList());
    }

    /**
     * 저장된 일간 박스오피스 조회
     */
    @Transactional
    public BoxOfficeListDTO<DailyBoxOfficeResponseDTO> getSavedDailyBoxOffice(String targetDt, int itemPerPage) {

        LocalDate requestDate = (targetDt == null || targetDt.isBlank())
                ? LocalDate.now().minusDays(1)
                : LocalDate.parse(targetDt, FMT);

        String resolvedTargetDt = requestDate.format(FMT);

        // 1) 요청 날짜로 시도
        List<DailyBoxOfficeEntity> rows = dailyRepo.findAllSortedByTargetDate(requestDate);

        // 2) fallback: 없으면 최신
        if (rows.isEmpty()) {
            List<DailyBoxOfficeEntity> latestRows = dailyRepo.findLatestSorted();
            if (!latestRows.isEmpty()) {
                LocalDate latestDate = latestRows.get(0).getTargetDate();
                if (!latestDate.equals(requestDate)) {
                    log.warn("[Daily][Fallback] 요청일 {} 데이터 없음 → 최신 {} 로 대체",
                            requestDate, latestDate);
                    rows = latestRows;
                    resolvedTargetDt = latestDate.format(FMT);
                }
            }
        }

        if (rows.isEmpty()) {
            throw new BaseException(
                    ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getHttpStatus(),
                    ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getMessage()
            );
        }


        List<DailyBoxOfficeResponseDTO> items = rows.stream()
                .limit(itemPerPage)
                .map(DailyBoxOfficeResponseDTO::fromEntity)
                .toList();

        return BoxOfficeListDTO.<DailyBoxOfficeResponseDTO>builder()
                .boxofficeType("일별")
                .targetDt(resolvedTargetDt)
                .items(items)
                .build();
    }

    /**
     * 저장된 주간 박스오피스 조회
     */
    @Transactional
    public BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO> getSavedWeeklyBoxOffice(
            String targetDt, String weekGb, int itemPerPage) {

        String yearWeek = Optional.ofNullable(targetDt)
                .filter(s -> !s.isBlank())
                .map(dt -> {
                    LocalDate d = LocalDate.parse(dt, FMT);
                    WeekFields wf = WeekFields.ISO;
                    int w = d.get(wf.weekOfWeekBasedYear());
                    int y = d.get(wf.weekBasedYear());
                    return String.format("%04dIW%02d", y, w);
                })
                .orElse(null);

        List<WeeklyBoxOfficeEntity> rows =
                (yearWeek != null) ? weeklyRepo.findAllSortedByYearWeek(yearWeek) : List.of();

        if (rows.isEmpty()) {
            rows = weeklyRepo.findLatestSorted();
            if (rows.isEmpty()) {
                throw new BaseException(
                        ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                        ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getMessage()
                );
            }
            yearWeek = rows.get(0).getYearWeekTime();
            log.warn("[Weekly][Fallback] 요청된 targetDt 데이터 없음 → 최신 yearWeek={} 사용", yearWeek);
        }

        List<WeeklyBoxOfficeResponseDTO> items = rows.stream()
                .limit(itemPerPage)
                .map(WeeklyBoxOfficeResponseDTO::fromEntity)
                .toList();

        return BoxOfficeListDTO.<WeeklyBoxOfficeResponseDTO>builder()
                .boxofficeType("주간")
                .targetDt(yearWeek)
                .items(items)
                .build();
    }


     /**
     * 저장된 일간 박스오피스 영화의 MovieDetailResDto 리스트 반환
     */
    @Transactional
    public MovieDetailResDto getDailyMovieDetailByMovieId(Long movieId) {

        // 1) Movie 1차 조회 (있으면 바로 사용)
        Movie movie = movieRepo.findById(movieId).orElse(null);

        if (movie == null) {
            // 로컬 Movie 없음 → 어떤 영화인지 알아내야 함.
            // Daily 박스오피스 테이블에서 movieId 와 연결된 row 가 있을 수도, 없을 수도 있음.
            List<DailyBoxOfficeEntity> latestDaily = dailyRepo.findLatestSorted();

            // movieId 로 직접 DailyBoxOfficeEntity 의 Movie FK 매칭 (존재할 때)
            DailyBoxOfficeEntity matchedByMovieEntity = latestDaily.stream()
                    .filter(d -> d.getMovie() != null && d.getMovie().getId().equals(movieId))
                    .findFirst()
                    .orElse(null);

            String titleForSearch;
            Integer yearForSearch;

            if (matchedByMovieEntity != null) {
                titleForSearch = matchedByMovieEntity.getMovieName();
                yearForSearch = extractYearSafe(matchedByMovieEntity.getOpenDate()); // "yyyy-MM-dd" or "yyyyMMdd"
            } else {
                throw new BaseException(
                        ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getHttpStatus(),
                        "영화가 로컬에 없고 박스오피스에서도 역추적할 수 없습니다. movieId=" + movieId
                );
            }

            log.info("[DailyDetail][AutoFetch] local Movie 없음. 제목='{}', year={}", titleForSearch, yearForSearch);

            // TMDB 검색
            movieService.searchMovieByTitleAndYear(titleForSearch, yearForSearch)
                    .ifPresentOrElse(found -> {
                                // 상세 저장 (actors/감독 등 포함)
                                movieService.fetchAndSaveMovieById(found.getId());
                            },
                            () -> {
                                throw new BaseException(
                                        ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getHttpStatus(),
                                        "TMDB 검색 실패: title=" + titleForSearch + ", year=" + yearForSearch
                                );
                            });

            // 다시 로컬에서 재조회 (방금 TMDB 저장)
            movie = movieRepo.findByTmdbMovieId(
                            movieService.searchMovieByTitleAndYear(titleForSearch, yearForSearch)
                                    .map(SearchMovieResponseDTO::getId)
                                    .orElse(null)
                    ).orElseThrow(() -> new BaseException(
                            ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getHttpStatus(),
                            "TMDB 저장 후 Movie 재조회 실패"
                    ));
        }

        // TMDB ID 확인 (신규 저장이라면 반드시 존재)
        Long tmdbId = movie.getTmdbMovieId();
        if (tmdbId == null) {
            throw new BaseException(
                    ErrorStatus.NOT_FOUND_DAILY_MOIVE.getHttpStatus(),
                    "해당 영화에 TMDB ID가 없습니다. movieId=" + movieId
            );
        }

        // 기준 날짜 = 어제
        LocalDate baseDate = LocalDate.now().minusDays(1);

        // 어제 날짜의 박스오피스 레코드 조회
        Optional<DailyBoxOfficeEntity> yesterdayOpt =
                dailyRepo.findByMovie_TmdbMovieIdAndTargetDate(tmdbId, baseDate);

        // 없으면 최신 fallback 에서 동일 영화
        DailyBoxOfficeEntity daily = yesterdayOpt.orElseGet(() -> {
            List<DailyBoxOfficeEntity> latestRows = dailyRepo.findLatestSorted();
            return latestRows.stream()
                    .filter(e -> e.getMovie() != null &&
                            tmdbId.equals(e.getMovie().getTmdbMovieId()))
                    .findFirst()
                    .orElseThrow(() -> new BaseException(
                            ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getHttpStatus(),
                            "해당 영화의 일간 박스오피스 데이터가 없습니다."
                    ));
        });
        return toMovieDetailResDto(movie);
    }

    /**
     * 저장된 주간 박스오피스 영화의 MovieDetailResDto 리스트 반환
     */
    @Transactional
    public MovieDetailResDto getWeeklyMovieDetailByMovieId(Long movieId, String weekGb) {

        // Movie 1차 조회 (없으면 나중에 박스오피스 기반 TMDB 검색 시도)
        Movie movie = movieRepo.findById(movieId).orElse(null);

        // 최신 yearWeek 결정 (weeklyRepo.findLatestSorted() 가 최신순 정렬이라고 가정)
        List<WeeklyBoxOfficeEntity> latestList = weeklyRepo.findLatestSorted();
        if (latestList.isEmpty()) {
            throw new BaseException(
                    ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                    "저장된 주간 박스오피스 데이터가 없습니다."
            );
        }
        String latestYearWeek = latestList.get(0).getYearWeekTime();

        // 최신 week의 동일 영화 레코드를 확보 (Movie 없을 때 제목/연도용)
        WeeklyBoxOfficeEntity matchedWeekly = latestList.stream()
                .filter(w -> w.getMovie() != null && w.getMovie().getId().equals(movieId))
                .findFirst()
                .orElse(null);

        // 로컬 Movie 없으면 → weekly 데이터로 제목/개봉연 찾아 TMDB 검색 & 저장
        if (movie == null) {
            if (matchedWeekly == null) {
                // Movie 자체가 없고, latest weekly 데이터에서도 연결 불가 → 더 이상 단서 없음
                throw new BaseException(
                        ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getHttpStatus(),
                        "Movie가 로컬에 없고 주간 박스오피스에서도 역추적 불가. movieId=" + movieId
                );
            }

            String titleForSearch = matchedWeekly.getMovieNm();
            int yearForSearch = extractYearSafe(matchedWeekly.getOpenDt()); // "yyyy-MM-dd" or "yyyyMMdd"
            log.info("[WeeklyDetail][AutoFetch] local Movie 없음. title='{}', year={}", titleForSearch, yearForSearch);

            // TMDB 검색
            Long fetchedTmdbId = movieService.searchMovieByTitleAndYear(titleForSearch, yearForSearch)
                    .map(SearchMovieResponseDTO::getId)
                    .orElseThrow(() -> new BaseException(
                            ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getHttpStatus(),
                            "TMDB 검색 실패: title=" + titleForSearch + ", year=" + yearForSearch
                    ));

            // 상세 저장
            movieService.fetchAndSaveMovieById(fetchedTmdbId);

            // 재조회 (tmdbMovieId 로)
            movie = movieRepo.findByTmdbMovieId(fetchedTmdbId)
                    .orElseThrow(() -> new BaseException(
                            ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getHttpStatus(),
                            "TMDB 저장 후 Movie 재조회 실패 tmdbId=" + fetchedTmdbId
                    ));
        }

        // TMDB ID 필수 검증
        Long tmdbId = movie.getTmdbMovieId();
        if (tmdbId == null) {
            throw new BaseException(
                    ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                    "해당 영화에 TMDB ID가 없습니다. movieId=" + movieId
            );
        }

        // 최신 yearWeek 에서 주간 박스오피스 레코드 조회 (없으면 fallback: 이미 latestList 에 같은 week 다 있음)
        WeeklyBoxOfficeEntity weeklyRecord = weeklyRepo
                .findByMovie_TmdbMovieIdAndYearWeekTime(tmdbId, latestYearWeek)
                .orElseGet(() ->
                        // 혹시 findBy... 가 null 이면 latestList 에서 직접 검색
                        latestList.stream()
                                .filter(w -> w.getMovie() != null &&
                                        tmdbId.equals(w.getMovie().getTmdbMovieId()))
                                .findFirst()
                                .orElseThrow(() -> new BaseException(
                                        ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                                        "최신 주간 데이터에서 해당 영화 레코드를 찾을 수 없습니다."
                                ))
                );
        return toMovieDetailResDto(movie);
    }


    /**
     * Movie 엔티티 → MovieDetailResDto 매핑 헬퍼
     */
    private MovieDetailResDto toMovieDetailResDto(Movie movie) {
        MovieDetailResDto dto = new MovieDetailResDto();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setTitleEn(movie.getTitleEn());
        dto.setOverview(movie.getOverview());
        dto.setPosterPath(movie.getPosterPath());
        dto.setBackdropPath(movie.getBackdropPath());
        dto.setVoteAverage(movie.getVoteAverage());
        dto.setOriginalLanguage(movie.getOriginalLanguage());
        dto.setIsLike(false); // TODO: 좋아요 연동

        // 장르
        List<String> genres = movieGenreRepository.findByMovie(movie)
                .stream()
                .map(mg -> mg.getGenreType().name())
                .toList();
        dto.setGenre(genres);

        // 배우 / 감독 / OTT : JSON 배열 파싱
        dto.setActors(readJsonArray(movie.getActors()));          // ["배우1","배우2",...]
        dto.setDirector(readJsonArray(movie.getDirectors()));     // ["감독1","감독2"...]
        dto.setOttProviders(readJsonArray(movie.getOttProviders())); // []

        dto.setRating(movie.getRating());

        // releaseDate (LocalDate → String "yyyy-MM-dd" 혹은 null)
        if (movie.getReleaseDate() != null) {
            dto.setReleaseDate(movie.getReleaseDate().toString());  // DTO 타입에 맞게
        } else {
            dto.setReleaseDate(null);
        }

        dto.setRuntime(movie.getRuntime());
        dto.setStatus(movie.getStatus());
        return dto;
    }


    private List<String> parseList(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.replaceAll("^\\[|]$", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String parseSingle(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.replaceAll("^\\[|]$", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private List<String> readJsonArray(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            // 과거 toString() 포맷 호환 (fallback)
            if (raw.startsWith("[") && raw.endsWith("]")) {
                return Arrays.stream(raw.substring(1, raw.length() - 1).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }
            return List.of();
        }
    }

    private int extractYearSafe(String openDate) {
        if (openDate == null || openDate.isBlank()) {
            return LocalDate.now().getYear();
        }
        // 가능한 포맷 2가지 시도
        String trimmed = openDate.trim();
        try {
            if (trimmed.length() == 10) { // yyyy-MM-dd
                return LocalDate.parse(trimmed, DateTimeFormatter.ISO_DATE).getYear();
            } else if (trimmed.length() == 8) { // yyyyMMdd
                return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyyMMdd")).getYear();
            }
        } catch (Exception ignored) { }
        return LocalDate.now().getYear();
    }

}
