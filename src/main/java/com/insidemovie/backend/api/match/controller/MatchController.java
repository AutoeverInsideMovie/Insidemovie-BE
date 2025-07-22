package com.insidemovie.backend.api.match.controller;

import com.insidemovie.backend.api.match.dto.WinnerHistoryDto;
import com.insidemovie.backend.api.match.service.MatchService;
import com.insidemovie.backend.api.movie.dto.MovieDetailSimpleResDto;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/match")
@Tag(name = "Match", description = "영화 대결 관련 API")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;

    // 영화 대결 투표
    @Operation(summary = "영화 대결 투표", description = "더 좋아하는 영화에 투표합니다.")
    @PostMapping("/vote/{movieId}")
    public ResponseEntity<ApiResponse<Void>> voteMatch(
            @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        matchService.voteMatch(movieId, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.SEND_VOTE_SUCCESS);
    }

    // 영화 대결 조회
    @Operation(summary = "대결 영화 조회", description = "대결중인 영화 내역을 조회합니다.")
    @GetMapping("/weekly-match")
    public ResponseEntity<ApiResponse<List<MovieDetailSimpleResDto>>> MatchDetail() {
        List<MovieDetailSimpleResDto> response = matchService.getMatchDetail();
        return ApiResponse.success(SuccessStatus.GET_MATCH_DETAIL_SUCCESS, response);
    }

    // 역대 우승 영화 조회
    @Operation(summary = "역대 우승 영화 조회", description = "영화 대결에서 우승한 영화 내역을 조회합니다.")
    @GetMapping("/winners")
    public ResponseEntity<ApiResponse<List<WinnerHistoryDto>>> winnerHistory() {
        List<WinnerHistoryDto> response = matchService.getWinnerHistory();
        return ApiResponse.success(SuccessStatus.GET_WINNER_SUCCESS, response);
    }
}
