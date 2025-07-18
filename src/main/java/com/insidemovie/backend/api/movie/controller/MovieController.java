package com.insidemovie.backend.api.movie.controller;

import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.service.MovieDetailService;
import com.insidemovie.backend.api.movie.service.MovieLikeService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies", description = "영화 관련 API")
@RequiredArgsConstructor
public class MovieController {
    private final MovieDetailService movieDetailService;
    private final MovieLikeService movieLikeService;

    @Operation(summary = "영화 상세 조회", description = "TMDB ID로 영화 상세정보를 조회합니다")
    @GetMapping("/detail/{tmdbId}")
    public ResponseEntity<ApiResponse<MovieDetailResDto>> getMovieDetail(@PathVariable Long tmdbId){
        MovieDetailResDto dto= movieDetailService.getMovieDetail(tmdbId);
        return ApiResponse.success(SuccessStatus.SEND_MOVIE_DETAIL_SUCCESS,dto);
    }

    // 영화 좋아요
    @Operation(summary = "영화 좋아요 클릭", description = "영화에 좋아요 또는 좋아요 취소를 합니다.")
    @PostMapping("/{movieId}/like-movie")
    public ResponseEntity<ApiResponse<Void>> toggleMovieLike(
            @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        movieLikeService.toggleMovieLike(movieId, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.SEND_MOVIE_LIKE_SUCCESS);
    }
}
