package com.insidemovie.backend.api.admin.controller;

import com.insidemovie.backend.api.admin.dto.AdminMemberDTO;
import com.insidemovie.backend.api.admin.dto.AdminReviewDTO;
import com.insidemovie.backend.api.admin.service.AdminService;
import com.insidemovie.backend.common.response.ApiResponse;
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

    @Operation(
            summary = "회원 목록 조회 API", description = "회원 목록을 조회합니다.")
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<Page<AdminMemberDTO>>> getMembers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<AdminMemberDTO> memberPage = adminService.getMembers(keyword, pageRequest);
        return ApiResponse.success(SuccessStatus.SEND_MEMBER_LIST_SUCCESS, memberPage);
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


}
