package com.insidemovie.backend.api.admin.service;

import com.insidemovie.backend.api.admin.dto.AdminMemberDTO;
import com.insidemovie.backend.api.admin.dto.AdminReportDTO;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.report.entity.Report;
import com.insidemovie.backend.api.report.repository.ReportRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;

    // 회원 목록 조회
    @Transactional
    public Page<AdminMemberDTO> getMembers(String keyword, Pageable pageable) {
        return memberRepository
                .findByEmailContainingOrNicknameContaining(keyword, keyword, pageable)
                .map(this::convertToDto);
    }

    private AdminMemberDTO convertToDto(Member member) {
        long reviewCount = reviewRepository.countByMember(member);  // 리뷰 수 조회

        return AdminMemberDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .reportCount(member.getReportCount())
                .authority(member.getAuthority().name())
                .createdAt(member.getCreatedAt())
                .reviewCount(reviewCount)
                .isBanned(member.isBanned())
                .build();
    }

    // 회원 정지
    @Transactional
    public void banMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
        member.setBanned(true);  // 정지 처리
    }

    // 회원 정지 해제
    @Transactional
    public void unbanMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
        member.setBanned(false); // 정지 해제
    }

    // 신고 목록 조회
    @Transactional
    public Page<AdminReportDTO> getAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    private AdminReportDTO convertToDto(Report report) {
        return AdminReportDTO.builder()
                .reportId(report.getId())
                .reviewId(report.getReview().getId())
                .reviewContent(report.getReview().getContent())
                .reporterId(report.getReporter().getId())
                .reporterNickname(report.getReporter().getNickname())
                .reportedMemberId(report.getReportedMember().getId())
                .reportedNickname(report.getReportedMember().getNickname())
                .reason(report.getReason())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }

}
