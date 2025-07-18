package com.insidemovie.backend.api.report.repository;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.report.entity.Report;
import com.insidemovie.backend.api.report.entity.ReportStatus;
import com.insidemovie.backend.api.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 동일 사용자가 동일 리뷰를 이미 신고했는지 확인
    boolean existsByReviewAndReporter(Review review, Member reporter);

    // 관리자용 전체 신고 페이징 조회
    Page<Report> findAll(Pageable pageable);

    // 미처리 신고 수
    long countByStatus(ReportStatus status);

    // 일별 신고 수
    @Query(value = """
        SELECT DATE(created_at) AS date, COUNT(*) AS count
        FROM report
        WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        GROUP BY DATE(created_at)
        ORDER BY date ASC
        """, nativeQuery = true)
    List<Object[]> countReportsDaily();

    // 월별 신고 수
    @Query(value = """
        SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS count
        FROM report
        WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
        GROUP BY month
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> countReportsMonthly();
}
