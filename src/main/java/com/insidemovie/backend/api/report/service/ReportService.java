package com.insidemovie.backend.api.report.service;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.report.entity.Report;
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
    public void reportReview(String reporterEmail, Long reviewId) {

        // 신고자 조회
        Member reporter = memberRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_REVIEW_EXCEPTION.getMessage()));

        // 중복 신고 방지
        if (reportRepository.existsByReviewAndMember(review, reporter)) {
            throw new BadRequestException(ErrorStatus.DUPLICATE_REPORT_EXCEPTION.getMessage());
        }

        reportRepository.save(
                Report.builder()
                        .review(review)
                        .member(reporter)
                        .build()
        );
    }
}
