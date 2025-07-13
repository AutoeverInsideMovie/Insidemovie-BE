package com.insidemovie.backend.api.report.entity;


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
@Table(name = "report", uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "member_id"})) // 동일 사용자의 중복 신고 방지
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;  // 신고한 리뷰

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;  // 신고한 사용자

    @Builder.Default
    private boolean isProcessed = false;  // 신고 처리 여부 (기본값 false)

}
