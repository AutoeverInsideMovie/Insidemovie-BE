package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionSummaryResponseDTO;
import com.insidemovie.backend.api.movie.service.MovieDetailService;
import com.insidemovie.backend.api.movie.service.MovieService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies", description = "영화 관련 API")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;
    private final MovieDetailService movieDetailService;

    @Operation(summary = "영화 상세 조회", description = "TMDB ID로 영화 상세정보를 조회합니다")
    @GetMapping("/detail/{tmdbId}")
    public ResponseEntity<ApiResponse<MovieDetailResDto>> getMovieDetail(@PathVariable Long tmdbId) {
        MovieDetailResDto dto = movieDetailService.getMovieDetail(tmdbId);
        return ApiResponse.success(SuccessStatus.SEND_MOVIE_DETAIL_SUCCESS, dto);
    }

    @Operation(
      summary = "영화에 저장된 감정 상태 값 조회",
      description = "영화에 저장된 5가지 감정 상태 값을 조회합니다."
    )
    @GetMapping("/emotions/{movieId}")
    public ResponseEntity<ApiResponse<MovieEmotionSummaryResponseDTO>> getMovieEmotions(
            @PathVariable Long movieId
    ) {
        MovieEmotionSummaryResponseDTO dto = movieService.getMovieEmotions(movieId);
        return ApiResponse.success(SuccessStatus.SEND_MOVIE_EMOTION_SUCCESS, dto);
    }
}
