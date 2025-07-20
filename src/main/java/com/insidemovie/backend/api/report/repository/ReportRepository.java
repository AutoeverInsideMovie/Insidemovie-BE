package com.insidemovie.backend.api.report.repository;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.report.entity.Report;
import com.insidemovie.backend.api.constant.ReportStatus;
import com.insidemovie.backend.api.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // 동일 사용자가 동일 리뷰를 이미 신고했는지 확인
    boolean existsByReviewAndReporter(Review review, Member reporter);

    // 관리자용 전체 신고 페이징 조회
    Page<Report> findAll(Pageable pageable);

    // 미처리 신고 수
    long countByStatus(ReportStatus status);

    // 누적 신고 수 (특정 시점까지)
    long countByCreatedAtLessThan(LocalDateTime dateTime);

    // 일별 신고 수
    @Query("""
        SELECT DATE(r.createdAt), COUNT(r)
        FROM Report r
        WHERE r.createdAt >= :start AND r.createdAt < :end
        GROUP BY DATE(r.createdAt)
        ORDER BY DATE(r.createdAt)
    """)
    List<Object[]> countReportsDaily(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 월별 신고 수
    @Query("""
        SELECT FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m'), COUNT(r)
        FROM Report r
        WHERE r.createdAt >= :start AND r.createdAt < :end
        GROUP BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m')
    """)
    List<Object[]> countReportsMonthly(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}
