package com.insidemovie.backend.api.admin.controller;

import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.common.exception.BadRequestException;
import com.insidemovie.backend.common.response.ApiResponse;
import com.insidemovie.backend.common.response.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final MemberRepository memberRepository;
    @GetMapping("/totalSub")
    public ResponseEntity<ApiResponse<Long>> getTotalSub() {
        Long total = null;
        try {
            total = memberRepository.count();
            return ApiResponse.success(SuccessStatus.CREATE_SAMPLE_SUCCESS, total);
        } catch (Exception e) {
            throw new BadRequestException("전체 회원 조회 오류 : " + e.getMessage());
        }
    }
}
