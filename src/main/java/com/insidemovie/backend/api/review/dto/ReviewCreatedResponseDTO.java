package com.insidemovie.backend.api.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewCreatedResponseDTO {

    private Long reviewId;
}
