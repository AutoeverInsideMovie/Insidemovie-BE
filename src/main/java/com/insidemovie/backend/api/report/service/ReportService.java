package com.insidemovie.backend.api.report.service;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.report.dto.ReportResponseDTO;
import com.insidemovie.backend.api.report.entity.Report;
import com.insidemovie.backend.api.constant.ReportReason;
import com.insidemovie.backend.api.constant.ReportStatus;
import com.insidemovie.backend.api.report.repository.ReportRepository;
import com.insidemovie.backend.api.review.entity.Review;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.BadRequestException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;

    // 리뷰 신고
    @Transactional
    public ReportResponseDTO reportReview(String reporterEmail, Long reviewId, ReportReason reason) {

        // 신고자 조회
        Member reporter = memberRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_REVIEW_EXCEPTION.getMessage()));

        // 신고당한 사용자(리뷰 작성자) 가져오기
        Member reportedMember = review.getMember();

        // 중복 신고 방지
        if (reportRepository.existsByReviewAndReporter(review, reporter)) {
            throw new BadRequestException(ErrorStatus.DUPLICATE_REPORT_EXCEPTION.getMessage());
        }

        // 리뷰, 회원 상태 업데이트
//        review.markReported();                   // 리뷰 isReported = true
        reportedMember.incrementReportCount();   // 피신고자 reportCount++

        return ReportResponseDTO.fromEntity(
                reportRepository.save(
                        Report.builder()
                                .review(review)
                                .reporter(reporter)
                                .reportedMember(reportedMember)
                                .reason(reason)
                                .status(ReportStatus.UNPROCESSED)
                                .build()
                )
        );
    }

    // 신고 수용 처리
    @Transactional
    public void acceptReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_REPORT_EXCEPTION.getMessage()));

        report.updateStatus(ReportStatus.ACCEPTED);

        Review review = report.getReview();
        review.markReported();
        review.conceal();
    }

    // 신고 기각 처리
    @Transactional
    public void rejectReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_REPORT_EXCEPTION.getMessage()));

        report.updateStatus(ReportStatus.REJECTED);
    }
    // 신고 미처리화
    @Transactional
    public void unprocessedReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_REPORT_EXCEPTION.getMessage()));

        report.updateStatus(ReportStatus.UNPROCESSED);
    }

}
