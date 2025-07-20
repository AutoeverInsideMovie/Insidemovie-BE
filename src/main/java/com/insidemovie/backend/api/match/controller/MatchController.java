package com.insidemovie.backend.api.match.controller;

import com.insidemovie.backend.api.match.service.MatchService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/match")
@Tag(name = "Match", description = "영화 대결 관련 API")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;

    @Operation(summary = "영화 대결 투표", description = "더 좋아하는 영화에 투표합니다.")
    @PostMapping("/vote/{movieId}")
    public ResponseEntity<ApiResponse<Void>> voteMatch(
            @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        matchService.voteMatch(movieId, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.SEND_VOTE_SUCCESS);
    }
}
