package com.insidemovie.backend.api.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyReviewResponseDTO {

    private Long reviewId;
    private String content;
    private LocalDateTime createdAt;

    private Long movieId;
}
