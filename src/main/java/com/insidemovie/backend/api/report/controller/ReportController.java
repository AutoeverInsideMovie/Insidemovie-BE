package com.insidemovie.backend.api.report.controller;

import com.insidemovie.backend.api.report.service.ReportService;
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
@Tag(name="Report", description = "Report 관련 API 입니다.")
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "리뷰 신고 API", description = "리뷰를 신고합니다.")
    @PostMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> reportReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {

        reportService.reportReview(userDetails.getUsername(), reviewId);

        return ApiResponse.success_only(SuccessStatus.REPORT_CREATE_SUCCESS);
    }
}
