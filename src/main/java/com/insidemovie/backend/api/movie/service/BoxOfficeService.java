package com.insidemovie.backend.api.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.insidemovie.backend.api.movie.dto.*;
import com.insidemovie.backend.api.movie.entity.DailyBoxOfficeEntity;
import com.insidemovie.backend.api.movie.entity.WeeklyBoxOfficeEntity;
import com.insidemovie.backend.api.movie.repository.DailyBoxOfficeRepository;
import com.insidemovie.backend.api.movie.repository.WeeklyBoxOfficeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoxOfficeService {

    @Value("${kobis.api.key}")
    private String kobisApiKey;

    private final DailyBoxOfficeRepository dailyRepo;
    private final WeeklyBoxOfficeRepository weeklyRepo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String DAILY_URL_JSON =
        "http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json";
    private static final String WEEKLY_URL_JSON =
        "http://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchWeeklyBoxOfficeList.json";

    // 일간 박스오피스 조회 및 저장
    @Transactional
    public BoxOfficeListDTO<DailyBoxOfficeResponseDTO> getDailyBoxOffice(BoxOfficeRequestDTO req) {
        LocalDate date = LocalDate.parse(req.getTargetDt(), FMT);
        log.info("[Service] DailyBoxOffice for {}", date);

        // 기존 데이터 삭제
        dailyRepo.deleteByTargetDate(date);

        // API 호출 후 엔티티 변환
        List<DailyBoxOfficeEntity> entities = fetchDailyFromApi(req, date);

        // 저장
        dailyRepo.saveAll(entities);

        // DTO 변환
        List<DailyBoxOfficeResponseDTO> items = entities.stream()
            .map(DailyBoxOfficeResponseDTO::fromEntity)
            .collect(Collectors.toList());

        // 응답 래퍼
        String showRange = date + "~" + date;
        return BoxOfficeListDTO.<DailyBoxOfficeResponseDTO>builder()
            .boxofficeType("일별")
            .showRange(showRange)
            .items(items)
            .build();
    }

    // 외부 API 호출하여 일간 엔티티 목록 생성
    private List<DailyBoxOfficeEntity> fetchDailyFromApi(BoxOfficeRequestDTO req, LocalDate date) {
        RestTemplate rest = new RestTemplate();
        String uri = UriComponentsBuilder.fromHttpUrl(DAILY_URL_JSON)
            .queryParam("key", kobisApiKey)
            .queryParam("targetDt", req.getTargetDt())
            .queryParam("itemPerPage", req.getItemPerPage())
            .toUriString();

        JsonNode listNode = rest.getForObject(uri, JsonNode.class)
            .path("boxOfficeResult")
            .path("dailyBoxOfficeList");

        return StreamSupport.stream(listNode.spliterator(), false)
            .limit(req.getItemPerPage())
            .map(node -> DailyBoxOfficeEntity.builder()
                .targetDate(date)
                .rnum(node.path("rnum").asText())
                .rank(node.path("rank").asText())
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
        LocalDate date = LocalDate.parse(req.getTargetDt(), FMT);
        log.info("[Service] WeeklyBoxOffice for {} (weekGb={})", date, req.getWeekGb());

        // 주차 계산
        WeekFields wf = WeekFields.ISO;
        int week = date.get(wf.weekOfWeekBasedYear());
        int year = date.get(wf.weekBasedYear());
        String yearWeek = String.format("%04dIW%02d", year, week);

        // 기존 데이터 삭제
        weeklyRepo.deleteByYearWeekTime(yearWeek);

        // API 호출 후 엔티티 변환
        List<WeeklyBoxOfficeEntity> entities = fetchWeeklyFromApi(req, yearWeek);

        // 저장
        weeklyRepo.saveAll(entities);

        // DTO 변환
        List<WeeklyBoxOfficeResponseDTO> items = entities.stream()
            .map(WeeklyBoxOfficeResponseDTO::fromEntity)
            .collect(Collectors.toList());

        // 응답 래퍼
        return BoxOfficeListDTO.<WeeklyBoxOfficeResponseDTO>builder()
            .boxofficeType("주간")
            .items(items)
            .build();
    }

    // 외부 API 호출하여 주간 엔티티 목록 생성
    private List<WeeklyBoxOfficeEntity> fetchWeeklyFromApi(BoxOfficeRequestDTO req, String yearWeek) {
        RestTemplate rest = new RestTemplate();
        String uri = UriComponentsBuilder.fromHttpUrl(WEEKLY_URL_JSON)
            .queryParam("key", kobisApiKey)
            .queryParam("targetDt", req.getTargetDt())
            .queryParam("weekGb", req.getWeekGb())
            .queryParam("itemPerPage", req.getItemPerPage())
            .toUriString();

        JsonNode listNode = rest.getForObject(uri, JsonNode.class)
            .path("boxOfficeResult")
            .path("weeklyBoxOfficeList");

        return StreamSupport.stream(listNode.spliterator(), false)
            .limit(req.getItemPerPage())
            .map(node -> WeeklyBoxOfficeEntity.builder()
                .yearWeekTime(yearWeek)
                .rnum(node.path("rnum").asText())
                .rank(node.path("rank").asText())
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
                .build())
            .collect(Collectors.toList());
    }
}
