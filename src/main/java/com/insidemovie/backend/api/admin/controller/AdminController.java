package com.insidemovie.backend.api.admin.controller;

import com.insidemovie.backend.api.admin.dto.AdminDashboardDTO;
import com.insidemovie.backend.api.admin.dto.AdminMemberDTO;
import com.insidemovie.backend.api.admin.dto.AdminReportDTO;
import com.insidemovie.backend.api.admin.service.AdminService;
import com.insidemovie.backend.api.report.service.ReportService;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.PageResult;
import com.insidemovie.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Tag(name="ADMIN", description = "Admin 관련 API 입니다.")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;

    @Operation(
            summary = "회원 목록 조회 API", description = "회원 목록을 조회합니다.")
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<PageResult<AdminMemberDTO>>> getMembers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<AdminMemberDTO> memberPage = adminService.getMembers(keyword, pageRequest);
        return ApiResponse.success(SuccessStatus.SEND_MEMBER_LIST_SUCCESS, PageResult.of(memberPage));
    }

    @Operation(summary = "회원 정지", description = "특정 회원을 정지시킵니다.")
    @PatchMapping("/members/{memberId}/ban")
    public ResponseEntity<ApiResponse<Void>> banMember(@PathVariable Long memberId) {
        adminService.banMember(memberId);
        return ApiResponse.success_only(SuccessStatus.MEMBER_BAN_SUCCESS);
    }

    @Operation(summary = "회원 정지 해제", description = "특정 회원의 정지를 해제합니다.")
    @PatchMapping("/members/{memberId}/unban")
    public ResponseEntity<ApiResponse<Void>> unbanMember(@PathVariable Long memberId) {
        adminService.unbanMember(memberId);
        return ApiResponse.success_only(SuccessStatus.MEMBER_UNBAN_SUCCESS);
    }

    @Operation(summary = "신고 목록 조회", description = "전체 리뷰 신고 목록을 조회합니다.")
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<PageResult<AdminReportDTO>>> getReportList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<AdminReportDTO> reportPage = adminService.getAllReports(pageRequest);
        return ApiResponse.success(SuccessStatus.SEND_REPORT_LIST_SUCCESS, PageResult.of(reportPage));
    }

    @Operation(summary = "신고 수용", description = "신고를 수용합니다.")
    @PatchMapping("/reports/{reportId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptReport(@PathVariable Long reportId) {
        reportService.acceptReport(reportId);
        return ApiResponse.success_only(SuccessStatus.REPORT_ACCEPTED);
    }

    @Operation(summary = "신고 기각", description = "신고를 기각합니다.")
    @PatchMapping("/reports/{reportId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectReport(@PathVariable Long reportId) {
        reportService.rejectReport(reportId);
        return ApiResponse.success_only(SuccessStatus.REPORT_REJECTED);
    }

    @Operation(summary = "신고 보류", description = "신고를 보류합니다.")
    @PatchMapping("/reports/{reportId}/unprocessed")
    public ResponseEntity<ApiResponse<Void>> unprocessedReport(@PathVariable Long reportId) {
        reportService.unprocessedReport(reportId);
        return ApiResponse.success_only(SuccessStatus.REPORT_UNPROCESSED);
    }

    @Operation(summary = "관리자 대시보드 요약 정보", description = "회원 수, 리뷰 수 등 요약 정보를 반환합니다.")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDTO>> getDashboard() {
        AdminDashboardDTO dto = adminService.getDashboardSummary();
        return ApiResponse.success(SuccessStatus.SEND_DASHBOARD_SUCCESS, dto);
    }


}
