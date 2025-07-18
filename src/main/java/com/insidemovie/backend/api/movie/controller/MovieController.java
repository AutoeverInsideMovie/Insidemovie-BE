package com.insidemovie.backend.api.movie.controller;


import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.dto.PageResDto;


import com.insidemovie.backend.api.member.dto.EmotionAvgDTO;
import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionSummaryResponseDTO;
import com.insidemovie.backend.api.movie.service.MovieDetailService;
import com.insidemovie.backend.api.movie.service.MovieLikeService;

import com.insidemovie.backend.api.movie.service.MovieService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
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
    private final MovieLikeService movieLikeService;


    @Operation(summary = "영화 상세 조회", description = "TMDB ID로 영화 상세정보를 조회합니다")
    @GetMapping("/detail/{tmdbId}")
    public ResponseEntity<ApiResponse<MovieDetailResDto>> getMovieDetail(
            @PathVariable Long tmdbId,
            @AuthenticationPrincipal UserDetails userDetails) {

        MovieDetailResDto dto;

        if (userDetails != null) {
            dto = movieDetailService.getMovieDetail(tmdbId, userDetails.getUsername());
        } else {
            dto = movieDetailService.getMovieDetail(tmdbId);
        }

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

    @Operation(summary = "영화 감정 평균 조회", description = "해당 영화에 작성된 모든 리뷰의 감정 평균과 대표 감정을 조회합니다")
    @GetMapping("/{movieId}/emotion-summary")
    public ResponseEntity<ApiResponse<EmotionAvgDTO>> getMovieEmotionSummary(@PathVariable Long movieId) {
        EmotionAvgDTO summary = movieService.getMovieEmotionSummary(movieId);
        return ApiResponse.success(SuccessStatus.SEND_EMOTION_SUMMARY_SUCCESS, summary);
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

    @Operation(summary = "영화 타이틀 검색", description = "타이틀로 영화를 검색합니다.")
    @GetMapping("/search/title")
    public ResponseEntity<ApiResponse<PageResDto<MovieSearchResDto>>> MovieSearchTitle(@RequestParam String title,@RequestParam int page, @RequestParam int pageSize){
        PageResDto<MovieSearchResDto> result = movieService.movieSearchTitle(title,page, pageSize);
        return ApiResponse.success(SuccessStatus.SEARCH_MOVIES_SUCCESS,result);
    }
    /*
     * TODO: 영화 장르와, 타이틀로 검색했을때 검색되도록
     *   - "액"이 포함된 영화 타이틀을 검색하고 싶어도 액션으로 인식되어 액션 영화 나옴
     *   - 수정 방안 생각중
     */
    @Operation(summary = "영화 검색", description = "영화를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResDto<MovieSearchResDto>>> MovieSearchGenre(@RequestParam String q, @RequestParam int page, @RequestParam int pageSize){
        PageResDto<MovieSearchResDto> result = movieService.searchByQuery(q,page, pageSize);
        return ApiResponse.success(SuccessStatus.SEARCH_MOVIES_SUCCESS,result);
    }


}

