package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeListDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.DailyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.WeeklyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.service.BoxOfficeService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/boxoffice")
@RequiredArgsConstructor
@Slf4j
public class BoxOfficeController {

    private final BoxOfficeService boxOfficeService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 저장된 일간 박스오피스 조회.
     * targetDt 없으면: 어제 날짜로 자동 세팅.
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<BoxOfficeListDTO<DailyBoxOfficeResponseDTO>>> getDaily(
            @RequestParam(value = "targetDt", required = false) String targetDt,
            @RequestParam(defaultValue = "10") Integer itemPerPage
    ) {
        String resolved = (targetDt == null || targetDt.isBlank())
                ? LocalDate.now().minusDays(1).format(FMT)
                : targetDt;
        log.info("[Controller] resolved daily targetDt={}", resolved);

        BoxOfficeListDTO<DailyBoxOfficeResponseDTO> dto =
                boxOfficeService.getSavedDailyBoxOffice(resolved, itemPerPage);

        return ApiResponse.success(SuccessStatus.SEND_DAILY_BOXOFFICE_SUCCESS, dto);
    }

    /**
     * 일간 박스오피스 영화 한 편의 상세정보 조회
     */
    @GetMapping("/daily/detail")
    public ResponseEntity<ApiResponse<MovieDetailResDto>> getDailyMovieDetail(
            @RequestParam Long movieId,
            @RequestParam(value = "targetDt", required = false) String targetDt
    ) {
        String resolved = (targetDt == null || targetDt.isBlank())
                ? LocalDate.now().minusDays(1).format(FMT)
                : targetDt;

        MovieDetailResDto dto =
                boxOfficeService.getDailyMovieDetailByMovieId(movieId, resolved);

        return ApiResponse.success(SuccessStatus.SEND_BOXOFFICE_MOVIE_DETAIL_SUCCESS, dto);
    }

    /**
     * 저장된 주간 박스오피스 조회.
     * targetDt 없으면: 지난주 날짜 기반 최신 yearWeek 자동 결정 (Service 내부 로직 활용)
     */
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO>>> getWeekly(
            @RequestParam(value = "targetDt", required = false) String targetDt,
            @RequestParam(defaultValue = "0") String weekGb,
            @RequestParam(defaultValue = "10") Integer itemPerPage
    ) {
        BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO> dto =
                boxOfficeService.getSavedWeeklyBoxOffice(targetDt, weekGb, itemPerPage);

        return ApiResponse.success(SuccessStatus.SEND_WEEKLY_BOXOFFICE_SUCCESS, dto);
    }

    /**
     * 주간 박스오피스 특정 영화 상세
     */
    @GetMapping("/weekly/detail")
    public ResponseEntity<ApiResponse<MovieDetailResDto>> getWeeklyMovieDetail(
            @RequestParam Long movieId,
            @RequestParam(value = "targetDt", required = false) String targetDt,
            @RequestParam(defaultValue = "0") String weekGb
    ) {
        // targetDt 없으면 지난주 날짜 사용
        String resolved = (targetDt == null || targetDt.isBlank())
                ? LocalDate.now().minusWeeks(1).format(FMT)
                : targetDt;

        MovieDetailResDto dto =
                boxOfficeService.getWeeklyMovieDetailByMovieId(movieId, resolved, weekGb);

        return ApiResponse.success(SuccessStatus.SEND_BOXOFFICE_MOVIE_DETAIL_SUCCESS, dto);
    }
}
