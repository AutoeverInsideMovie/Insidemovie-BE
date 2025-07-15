package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.dto.BoxOfficeListDTO;
import com.insidemovie.backend.api.movie.dto.BoxOfficeRequestDTO;
import com.insidemovie.backend.api.movie.dto.DailyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.WeeklyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.service.BoxOfficeService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boxoffice")
@RequiredArgsConstructor
public class BoxOfficeController {

    private final BoxOfficeService boxOfficeService;

    // 일간 박스오피스 조회
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<BoxOfficeListDTO<DailyBoxOfficeResponseDTO>>> getDaily(
        @RequestParam(defaultValue = "") String targetDt,
        @RequestParam(defaultValue = "10") Integer itemPerPage,
        @RequestParam(defaultValue = "N") String multiMovieYn,
        @RequestParam(required = false) String repNationCd,
        @RequestParam(required = false) String wideAreaCd
    ) {
        BoxOfficeRequestDTO req = BoxOfficeRequestDTO.builder()
            .targetDt(targetDt)
            .itemPerPage(itemPerPage)
            .multiMovieYn(multiMovieYn)
            .repNationCd(repNationCd)
            .wideAreaCd(wideAreaCd)
            .build();

        BoxOfficeListDTO<DailyBoxOfficeResponseDTO> response =
            boxOfficeService.getDailyBoxOffice(req);

        return ApiResponse.success(
            SuccessStatus.SEND_DAILY_BOXOFFICE_SUCCESS,
            response
        );
    }

    // 주간 박스오피스 조회
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO>>> getWeekly(
        @RequestParam(defaultValue = "") String targetDt,
        @RequestParam(defaultValue = "0") String weekGb,
        @RequestParam(defaultValue = "10") Integer itemPerPage
    ) {
        BoxOfficeRequestDTO req = BoxOfficeRequestDTO.builder()
            .targetDt(targetDt)
            .weekGb(weekGb)
            .itemPerPage(itemPerPage)
            .build();

        BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO> response =
            boxOfficeService.getWeeklyBoxOffice(req);

        return ApiResponse.success(
            SuccessStatus.SEND_WEEKLY_BOXOFFICE_SUCCESS,
            response
        );
    }
}
