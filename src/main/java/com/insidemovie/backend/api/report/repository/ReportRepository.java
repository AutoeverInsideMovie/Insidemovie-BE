package com.insidemovie.backend.api.report.repository;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.report.entity.Report;
import com.insidemovie.backend.api.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 동일 사용자가 동일 리뷰를 이미 신고했는지 확인
    boolean existsByReviewAndReporter(Review review, Member reporter);

    // 관리자용 전체 신고 페이징 조회
    Page<Report> findAll(Pageable pageable);
}
