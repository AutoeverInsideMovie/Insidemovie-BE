package com.insidemovie.backend.api.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeListDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeRequestDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.DailyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.WeeklyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.boxoffice.DailyBoxOfficeEntity;
import com.insidemovie.backend.api.movie.entity.boxoffice.WeeklyBoxOfficeEntity;
import com.insidemovie.backend.api.movie.repository.DailyBoxOfficeRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.movie.repository.WeeklyBoxOfficeRepository;
import com.insidemovie.backend.common.exception.BaseException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoxOfficeService {

    private final MovieService movieService;
    @Value("${kobis.api.key}")         private String kobisApiKey;
    @Value("${tmdb.api.base-url}")    private String tmdbBaseUrl;
    @Value("${tmdb.api.key}")         private String tmdbApiKey;
    @Value("${tmdb.api.language}")    private String tmdbLanguage;

    private final DailyBoxOfficeRepository dailyRepo;
    private final WeeklyBoxOfficeRepository weeklyRepo;
    private final MovieRepository movieRepo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String DAILY_URL_JSON =
        "http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json";
    private static final String WEEKLY_URL_JSON =
        "http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchWeeklyBoxOfficeList.json";
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 일간 박스오피스 조회 및 저장
    @Transactional
    public BoxOfficeListDTO<DailyBoxOfficeResponseDTO> getDailyBoxOffice(BoxOfficeRequestDTO req) {
        // 어제 날짜 계산
        LocalDate date = LocalDate.now().minusDays(1);
        String targetDt = date.format(FMT);

        // 기존 데이터 삭제
        dailyRepo.deleteByTargetDate(date);

        // 변경된 호출
        List<DailyBoxOfficeEntity> entities = fetchDailyFromApi(date, req.getItemPerPage());

        // 저장·DTO 변환·예외 처리
        dailyRepo.saveAll(entities);
        
        entities.forEach(e -> {
            movieService.searchMovieByTitleAndYear(
                    e.getMovieName(),
                    LocalDate.parse(e.getOpenDate(), ISO_FMT).getYear()
                )
                .ifPresent(dto -> {
                    // ➌ TMDB 상세정보 먼저 저장
                    movieService.fetchAndSaveMovieById(dto.getId());

                    // ➍ 저장된 Movie 엔티티 조회
                    Movie movie = movieRepo.findByTmdbMovieId(dto.getId())
                        .orElseThrow(() -> new IllegalStateException(
                            "Movie not found for TMDB ID=" + dto.getId()));

                    // ➎ DailyBoxOfficeEntity 와 연관 설정
                    e.setMovie(movie);

                    log.info("[연동완료] {} ({}) → TMDB ID={}",
                        e.getMovieName(), e.getMovieCd(), dto.getId());
                });
        });
        
        List<DailyBoxOfficeResponseDTO> items = entities.stream()
            .map(DailyBoxOfficeResponseDTO::fromEntity)
            .collect(Collectors.toList());

        if (items.isEmpty()) {
            throw new BaseException(
                ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getHttpStatus(),
                ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getMessage()
            );
        }

        String showRange = targetDt + "~" + targetDt;
        return BoxOfficeListDTO.<DailyBoxOfficeResponseDTO>builder()
            .boxofficeType("일별")

            .items(items)
            .build();
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
    public BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO> getWeeklyBoxOffice(BoxOfficeRequestDTO req) {
        // 1) 어제 대신 '지난주' 날짜 구하기
        LocalDate lastWeekDate = LocalDate.now().minusWeeks(1);
        String targetDt = lastWeekDate.format(FMT);

        // 2) ISO 주차 기준으로 '년+주차' 문자열 생성 (예: 2025IW28)
        WeekFields wf = WeekFields.ISO;
        int week = lastWeekDate.get(wf.weekOfWeekBasedYear());
        int year = lastWeekDate.get(wf.weekBasedYear());
        String yearWeek = String.format("%04dIW%02d", year, week);

        log.info("[Service] WeeklyBoxOffice for last week {} (yearWeek={})", targetDt, yearWeek);

        // 3) 기존 DB 삭제
        weeklyRepo.deleteByYearWeekTime(yearWeek);

        // 4) API 호출 (지난주 targetDt, req.getWeekGb(), req.getItemPerPage(), yearWeek 사용)
        List<WeeklyBoxOfficeEntity> entities =
            fetchWeeklyFromApi(lastWeekDate, req.getWeekGb(), req.getItemPerPage(), yearWeek);

        // 5) 저장·DTO 변환
        weeklyRepo.saveAll(entities);

        entities.forEach(e -> {
            movieService.searchMovieByTitleAndYear(
                    e.getMovieNm(),
                    // ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    LocalDate.parse(e.getOpenDt(), ISO_FMT).getYear()
                )
                .ifPresent(dto -> {
                    movieService.fetchAndSaveMovieById(dto.getId());
                    Movie movie = movieRepo.findByTmdbMovieId(dto.getId())
                        .orElseThrow(() -> new IllegalStateException(
                            "Movie not found for TMDB ID=" + dto.getId()));
                    e.setMovie(movie);
                    log.info("[연동완료] {} ({}) → TMDB ID={}",
                        e.getMovieNm(), e.getMovieCd(), dto.getId());
                });
            });

        List<WeeklyBoxOfficeResponseDTO> items = entities.stream()
            .map(WeeklyBoxOfficeResponseDTO::fromEntity)
            .collect(Collectors.toList());

        if (items.isEmpty()) {
            throw new BaseException(
                ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getMessage()
            );
        }

        // 6) 응답
        return BoxOfficeListDTO.<WeeklyBoxOfficeResponseDTO>builder()
            .boxofficeType("주간")
            .items(items)
            .build();
    }

    // 외부 API 호출하여 주간 엔티티 목록 생성
    private List<WeeklyBoxOfficeEntity> fetchWeeklyFromApi(
            LocalDate date,
            String weekGb,
            int itemPerPage,
            String yearWeek
    ) {
        String targetDt = date.format(FMT);
        RestTemplate rest = new RestTemplate();
        String uri = UriComponentsBuilder
            .fromHttpUrl(WEEKLY_URL_JSON)
            .queryParam("key", kobisApiKey)
            .queryParam("targetDt", targetDt)    // 지난 주 기준 날짜
            .queryParam("weekGb", weekGb)        // 요청으로 넘어온 대로 사용
            .queryParam("itemPerPage", itemPerPage)
            .toUriString();

        JsonNode listNode = rest.getForObject(uri, JsonNode.class)
            .path("boxOfficeResult")
            .path("weeklyBoxOfficeList");

        return StreamSupport.stream(listNode.spliterator(), false)
            .limit(itemPerPage)
            .map(node -> WeeklyBoxOfficeEntity.builder()
                .yearWeekTime(yearWeek)                // DB에 저장할 연주차
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
        LocalDate date = LocalDate.parse(targetDt, FMT);
        Pageable pageable = PageRequest.of(0, itemPerPage, Sort.by("movieRank"));
        Page<DailyBoxOfficeEntity> page = dailyRepo.findByTargetDate(date, pageable);

        List<DailyBoxOfficeResponseDTO> items = page.stream()
            .map(DailyBoxOfficeResponseDTO::fromEntity)
            .collect(Collectors.toList());

        if (items.isEmpty()) {
            throw new BaseException(
                ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getHttpStatus(),
                ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getMessage()
            );
        }

        return BoxOfficeListDTO.<DailyBoxOfficeResponseDTO>builder()
            .boxofficeType("일별")
            .targetDt(targetDt)
            .items(items)
            .build();
    }

    /**
     * 저장된 주간 박스오피스 조회
     */
    @Transactional
    public BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO> getSavedWeeklyBoxOffice(
            String targetDt, String weekGb, int itemPerPage
    ) {
        // 파라미터가 없으면 가장 최근 저장된 yearWeekTime 조회
        String yearWeek = Optional.ofNullable(targetDt)
            .filter(dt -> !dt.isBlank())
            .map(dt -> {
                LocalDate date = LocalDate.parse(dt, FMT);
                WeekFields wf = WeekFields.ISO;
                int week = date.get(wf.weekOfWeekBasedYear());
                int year = date.get(wf.weekBasedYear());
                return String.format("%04dIW%02d", year, week);
            })
            .orElseGet(() ->
                weeklyRepo.findFirstByOrderByYearWeekTimeDesc()
                          .map(WeeklyBoxOfficeEntity::getYearWeekTime)
                          .orElseThrow(() ->
                              new BaseException(
                                  ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                                  "저장된 박스오피스 데이터가 없습니다."
                              ))
            );

        // 페이징 조회
        Pageable pageReq = PageRequest.of(0, itemPerPage, Sort.by("movieRank"));
        Page<WeeklyBoxOfficeEntity> page = weeklyRepo.findByYearWeekTime(yearWeek, pageReq);

        List<WeeklyBoxOfficeResponseDTO> items = page.stream()
            .map(WeeklyBoxOfficeResponseDTO::fromEntity)
            .collect(Collectors.toList());

        if (items.isEmpty()) {
            throw new BaseException(
                ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getMessage()
            );
        }

        return BoxOfficeListDTO.<WeeklyBoxOfficeResponseDTO>builder()
            .boxofficeType("주간")
            .targetDt(yearWeek)
            .items(items)
            .build();
    }
}
