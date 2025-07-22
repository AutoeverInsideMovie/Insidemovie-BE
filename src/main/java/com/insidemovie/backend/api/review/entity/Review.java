package com.insidemovie.backend.api.review.entity;

import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.report.entity.Report;
import com.insidemovie.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Table(name = "review")
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    private double rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    private long likeCount;

    private LocalDateTime watchedAt;
    private boolean spoiler;

    @Builder.Default
    @Column(name = "is_reported", nullable = false)
    private boolean isReported = false;  // 신고여부

    @Builder.Default
    @Column(name = "is_concealed", nullable = false)
    private boolean isConcealed = false;  // 삭제여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private Emotion emotion;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReviewLike> likes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Report> reports = new ArrayList<>();

    public void modify(String content, double rating, boolean spoiler, LocalDateTime watchedAt) {
        this.content = content;
        this.rating = rating;
        this.spoiler = spoiler;
        this.watchedAt = watchedAt;
    }

    // 신고가 등록되면 호출
    public void markReported() {
        this.isReported = true;
    }

    // 리뷰 숨김 처리
    public void conceal() {
        this.isConcealed = true;
    }

}
