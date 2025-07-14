package com.insidemovie.backend.api.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewCreateDTO {

    private Long reviewId;
    private Long movieId;
    private String content;
    private double rating;
    private boolean spoiler;
    private LocalDateTime watchedAt;
}
