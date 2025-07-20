package com.insidemovie.backend.api.report.entity;


import com.insidemovie.backend.api.constant.ReportReason;
import com.insidemovie.backend.api.constant.ReportStatus;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.review.entity.Review;
import com.insidemovie.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@Table(name = "report", uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "reporter_id"})) // 동일 사용자의 중복 신고 방지
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    // 신고 대상 리뷰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    // 신고한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private Member reporter;

    // 신고당한(피신고) 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_member_id", nullable = false)
    private Member reportedMember;

    // 처리 상태: 미처리, 수용, 각하
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status = ReportStatus.UNPROCESSED;

    // 신고 사유
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;

    // 상태 변경용 메서드
    public void updateStatus(ReportStatus newStatus) {
                this.status = newStatus;
    }

}
