package com.insidemovie.backend.api.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponseDTO {

    private Long reviewId;
    private String content;
    private double rating;
    private boolean spoiler;
    private LocalDateTime createdAt; // 리뷰 작성일

    private int likeCount;

    private boolean myReview;  // 내가 작성한 리뷰면 true
    private boolean modify; // 내가 수정한 리뷰면 true
    private boolean myLike;  // 내가 좋아요 누른 리뷰

    private String nickname;

    private Long memberId;  // 작성자 ID
    private Long movieId;

    private EmotionDTO emotion; // 감정 상태 DTO

    private Boolean isReported;
    private Boolean isConcealed;
}
