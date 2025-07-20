package com.insidemovie.backend.api.movie.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeListDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeRequestDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.DailyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.WeeklyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.boxoffice.DailyBoxOfficeEntity;
import com.insidemovie.backend.api.movie.entity.boxoffice.WeeklyBoxOfficeEntity;
import com.insidemovie.backend.api.movie.repository.DailyBoxOfficeRepository;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.movie.repository.WeeklyBoxOfficeRepository;
import com.insidemovie.backend.common.exception.BaseException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Collections;
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
    public MovieDetailResDto getDailyMovieDetailByMovieId(Long movieId, String targetDt) {
        // 1) movieId → Movie 엔티티 조회
        Movie movie = movieRepo.findById(movieId)
            .orElseThrow(() -> new BaseException(
                ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getHttpStatus(),
                String.format("영화를 찾을 수 없습니다. movieId=%d", movieId)
            ));

        // 2) Movie 에서 tmdbId 확보
        Long tmdbId = movie.getTmdbMovieId();
        if (tmdbId == null) {
            throw new BaseException(
                ErrorStatus.NOT_FOUND_DAILY_MOIVE.getHttpStatus(),
                "해당 영화에 연동된 TMDB ID가 없습니다."
            );
        }

        // 3) targetDt → LocalDate 변환
        LocalDate date = LocalDate.parse(targetDt, FMT);

        // 4) 일간 박스오피스 레코드 조회
        DailyBoxOfficeEntity box = dailyRepo
            .findByMovie_TmdbMovieIdAndTargetDate(tmdbId, date)
            .orElseThrow(() -> new BaseException(
                ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getHttpStatus(),
                String.format("해당 날짜(%s)의 일간 박스오피스 정보를 찾을 수 없습니다.", targetDt)
            ));

        // 5) MovieDetailResDto 변환
        return toMovieDetailResDto(movie);
    }

    /**
     * 저장된 주간 박스오피스 영화의 MovieDetailResDto 리스트 반환
     */
    @Transactional
    public MovieDetailResDto getWeeklyMovieDetailByMovieId(
            Long movieId, String targetDt, String weekGb
    ) {
        // movieId → Movie 엔티티 조회
        Movie movie = movieRepo.findById(movieId)
            .orElseThrow(() -> new BaseException(
                ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getHttpStatus(),
                String.format("영화를 찾을 수 없습니다. movieId=%d", movieId)
            ));

        // Movie 에서 tmdbId 확보
        Long tmdbId = movie.getTmdbMovieId();
        if (tmdbId == null) {
            throw new BaseException(
                ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                "해당 영화에 연동된 TMDB ID가 없습니다."
            );
        }

        // targetDt → yearWeek 계산 (없으면 최신)
        String yearWeek = Optional.ofNullable(targetDt)
            .filter(dt -> !dt.isBlank())
            .map(dt -> {
                LocalDate d = LocalDate.parse(dt, FMT);
                WeekFields wf = WeekFields.ISO;
                int w = d.get(wf.weekOfWeekBasedYear());
                int y = d.get(wf.weekBasedYear());
                return String.format("%04dIW%02d", y, w);
            })
            .orElseGet(() -> {
                List<WeeklyBoxOfficeEntity> latest = weeklyRepo.findLatestSorted();
                if (latest.isEmpty()) {
                    throw new BaseException(
                            ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                            "저장된 주간 박스오피스 데이터가 없습니다."
                    );
                }
                return latest.get(0).getYearWeekTime();
            });

        // 주간 박스오피스 레코드 조회
        WeeklyBoxOfficeEntity box = weeklyRepo
            .findByMovie_TmdbMovieIdAndYearWeekTime(tmdbId, yearWeek)
            .orElseThrow(() -> new BaseException(
                ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                String.format("연주차(%s)의 주간 박스오피스 정보를 찾을 수 없습니다.", yearWeek)
            ));

        // 5) MovieDetailResDto 변환
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
}
