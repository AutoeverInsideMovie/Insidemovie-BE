package com.insidemovie.backend.api.report.controller;

import com.insidemovie.backend.api.constant.ReportReason;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.report.dto.ReportResponseDTO;
import com.insidemovie.backend.api.report.service.ReportService;
import com.insidemovie.backend.common.exception.ForbiddenException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.ErrorStatus;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name="Report", description = "Report 관련 API 입니다.")
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final MemberRepository memberRepository;

    @Operation(
            summary = "리뷰 신고 API", description = "리뷰를 신고합니다.")
    @PostMapping("/{reviewId}")
    public  ResponseEntity<ApiResponse<ReportResponseDTO>> reportReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam ReportReason reason
            ) {

        Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 정지된 사용자 차단
        if (member.isBanned()) {
            throw new ForbiddenException(ErrorStatus.USER_BANNED_EXCEPTION.getMessage());
        }

        ReportResponseDTO dto = reportService.reportReview(userDetails.getUsername(), reviewId, reason);

        return ApiResponse.success(SuccessStatus.REPORT_CREATE_SUCCESS, dto);
    }
}
