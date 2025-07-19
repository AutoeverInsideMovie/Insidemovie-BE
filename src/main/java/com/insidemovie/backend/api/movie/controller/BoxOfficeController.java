package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeListDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.DailyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.WeeklyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.service.BoxOfficeService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/boxoffice")
@RequiredArgsConstructor
public class BoxOfficeController {

    private final BoxOfficeService boxOfficeService;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 일간 박스오피스 조회
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<BoxOfficeListDTO<DailyBoxOfficeResponseDTO>>> getDaily(
        @RequestParam(value="targetDt", required=false, defaultValue="") String targetDt,
        @RequestParam(defaultValue = "10") Integer itemPerPage
    ) {
        // 날짜를 입력하지 않으면 최신(어제)의 박스오피스 정보를 응답하도록 설정
        String defaultDt = (targetDt == null || targetDt.isBlank())
            ? LocalDate.now().minusDays(1).format(FMT)
            : targetDt;

        BoxOfficeListDTO<DailyBoxOfficeResponseDTO> response =
            boxOfficeService.getSavedDailyBoxOffice(defaultDt, itemPerPage);
        return ApiResponse.success(
            SuccessStatus.SEND_DAILY_BOXOFFICE_SUCCESS,
            response
        );
    }

    /**
     * 일간 박스오피스 영화 한 편의 상세정보 조회
     */
    @GetMapping("/daily/detail")
    public ResponseEntity<ApiResponse<MovieDetailResDto>> getDailyMovieDetail(
        @RequestParam Long movieId,
        @RequestParam(value="targetDt", required=false, defaultValue="") String targetDt
    ) {
        String defaultDt = targetDt.isBlank()
            ? LocalDate.now().minusDays(1).format(FMT)
            : targetDt;

        MovieDetailResDto dto = boxOfficeService
            .getDailyMovieDetailByMovieId(movieId, defaultDt);

        return ApiResponse.success(
            SuccessStatus.SEND_BOXOFFICE_MOVIE_DETAIL_SUCCESS,
            dto
        );
    }

    // 주간 박스오피스 조회
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO>>> getWeekly(
        @RequestParam(value = "targetDt", required = false, defaultValue = "") String targetDt,
        @RequestParam(defaultValue = "0") String weekGb,
        @RequestParam(defaultValue = "10") Integer itemPerPage
    ) {
        BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO> response =
            boxOfficeService.getSavedWeeklyBoxOffice(targetDt, weekGb, itemPerPage);
        return ApiResponse.success(
            SuccessStatus.SEND_WEEKLY_BOXOFFICE_SUCCESS,
            response
        );
    }

    /**
     * 주간 박스오피스 영화 한 편의 상세정보 조회
     */
    @GetMapping("/weekly/detail")
    public ResponseEntity<ApiResponse<MovieDetailResDto>> getWeeklyMovieDetail(
        @RequestParam Long movieId,
        @RequestParam(value="targetDt", required=false, defaultValue="") String targetDt,
        @RequestParam(defaultValue="0") String weekGb
    ) {
        String defaultDt = targetDt.isBlank()
            ? LocalDate.now().minusWeeks(1).format(FMT)
            : targetDt;

        MovieDetailResDto dto = boxOfficeService
            .getWeeklyMovieDetailByMovieId(movieId, defaultDt, weekGb);

        return ApiResponse.success(
            SuccessStatus.SEND_BOXOFFICE_MOVIE_DETAIL_SUCCESS,
            dto
        );
    }
}
