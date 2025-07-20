package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeListDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.DailyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.WeeklyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.service.BoxOfficeService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
            summary = "일간 박스오피스 조회",
            description = """
                    저장된 일간 박스오피스 목록을 반환합니다.  
                    - `targetDt` 미지정 시 **어제 날짜(YYYYMMDD)** 기준으로 조회  
                    - 데이터가 없으면 서비스 로직에서 최신 날짜로 fallback  
                    - `itemPerPage` 로 상위 N개 제한
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "박스오피스 데이터 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<BoxOfficeListDTO<DailyBoxOfficeResponseDTO>>> getDaily(
            @Parameter(
                    name = "targetDt",
                    description = "조회 기준 일자 (YYYYMMDD). 없으면 어제 날짜 사용.",
                    example = "20250718",
                    required = false,
                    in = ParameterIn.QUERY
            )
            @RequestParam(value = "targetDt", required = false) String targetDt,
            @Parameter(
                    name = "itemPerPage",
                    description = "최대 반환 개수",
                    example = "10",
                    in = ParameterIn.QUERY
            )
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
    @Operation(
            summary = "일간 박스오피스 영화 상세 조회",
            description = """
                    일간 박스오피스에 포함된 특정 영화의 상세정보를 반환합니다.  
                    - `movieId` 는 내부 DB Movie 엔티티의 PK  
                    - 서비스 내부에서 (어제 → 최신 fallback) 날짜 로직을 적용하여 해당 날짜의 박스오피스 기록 유효성 확인  
                    - Movie 로컬 데이터 없으면 제목/연도로 TMDB 검색 후 저장 (구현된 경우)  
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "영화 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "영화 또는 박스오피스 레코드 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/daily/detail")
    public ResponseEntity<ApiResponse<MovieDetailResDto>> getDailyMovieDetail(
            @Parameter(
                    name = "movieId",
                    description = "상세를 조회할 영화의 내부 PK",
                    required = true,
                    example = "3695",
                    in = ParameterIn.QUERY
            )
            @RequestParam Long movieId
    ) {
        MovieDetailResDto dto = boxOfficeService.getDailyMovieDetailByMovieId(movieId);
        return ApiResponse.success(SuccessStatus.SEND_BOXOFFICE_MOVIE_DETAIL_SUCCESS, dto);
    }

    /**
     * 저장된 주간 박스오피스 조회.
     */
    @Operation(
            summary = "주간 박스오피스 조회",
            description = """
                    저장된 주간 박스오피스 목록을 반환합니다.  
                    - `targetDt` (YYYYMMDD) 기준으로 연/주차 계산  
                    - 해당 주차 데이터 없으면 최신 주차(yearWeek)로 fallback  
                    - `weekGb`: 0=주간 / 1=주말 / 2=주중 (KOBIS 규칙과 매핑)  
                    - `itemPerPage` 로 상위 N개 제한
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주간 박스오피스 데이터 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO>>> getWeekly(
            @Parameter(
                    name = "targetDt",
                    description = "기준 일자 (YYYYMMDD). 없으면 최신 주차로 fallback.",
                    example = "20250711",
                    required = false,
                    in = ParameterIn.QUERY
            )
            @RequestParam(value = "targetDt", required = false) String targetDt,
            @Parameter(
                    name = "weekGb",
                    description = "주간 구분 (0: 주간, 1: 주말, 2: 주중)",
                    example = "0",
                    in = ParameterIn.QUERY
            )
            @RequestParam(defaultValue = "0") String weekGb,
            @Parameter(
                    name = "itemPerPage",
                    description = "최대 반환 개수",
                    example = "10",
                    in = ParameterIn.QUERY
            )
            @RequestParam(defaultValue = "10") Integer itemPerPage
    ) {
        BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO> dto =
                boxOfficeService.getSavedWeeklyBoxOffice(targetDt, weekGb, itemPerPage);

        return ApiResponse.success(SuccessStatus.SEND_WEEKLY_BOXOFFICE_SUCCESS, dto);
    }

    /**
     * 주간 박스오피스 특정 영화 상세
     */
    @Operation(
            summary = "주간 박스오피스 영화 상세 조회",
            description = """
                    최신 주차(yearWeek) 기준으로 특정 영화의 주간 박스오피스 상세정보를 반환합니다.  
                    - `movieId` 는 내부 Movie PK  
                    - 최신 주차 데이터에서 해당 영화 레코드 없으면 예외 발생  
                    - Movie 로컬 부재 시 (구현된 경우) TMDB 저장 로직 연동 가능  
                    - `weekGb` 는 (0:주간,1:주말,2:주중) 조회 참고용
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "영화 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "영화 또는 주간 박스오피스 레코드 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/weekly/detail")
    public ResponseEntity<ApiResponse<MovieDetailResDto>> getWeeklyMovieDetail(
            @Parameter(
                    name = "movieId",
                    description = "상세를 조회할 영화의 내부 PK",
                    required = true,
                    example = "3695",
                    in = ParameterIn.QUERY
            )
            @RequestParam Long movieId,
            @Parameter(
                    name = "weekGb",
                    description = "주간 구분 (0: 주간, 1: 주말, 2: 주중)",
                    example = "0",
                    in = ParameterIn.QUERY
            )
            @RequestParam(defaultValue = "0") String weekGb
    ) {
        MovieDetailResDto dto = boxOfficeService.getWeeklyMovieDetailByMovieId(movieId, weekGb);
        return ApiResponse.success(SuccessStatus.SEND_BOXOFFICE_MOVIE_DETAIL_SUCCESS, dto);
    }
}