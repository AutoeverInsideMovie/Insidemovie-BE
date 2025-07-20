package com.insidemovie.backend.api.report.controller;

import com.insidemovie.backend.api.report.dto.ReportRequestDTO;
import com.insidemovie.backend.api.report.dto.ReportResponseDTO;
import com.insidemovie.backend.api.report.service.ReportService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(
            summary = "리뷰 신고 API", description = "리뷰를 신고합니다.")
    @PostMapping("/{reviewId}")
    public  ResponseEntity<ApiResponse<ReportResponseDTO>> reportReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ReportRequestDTO reportRequestDTO
            ) {

        ReportResponseDTO dto = reportService.reportReview(userDetails.getUsername(), reviewId, reportRequestDTO.getReason());

        return ApiResponse.success(SuccessStatus.REPORT_CREATE_SUCCESS, dto);
    }
}
