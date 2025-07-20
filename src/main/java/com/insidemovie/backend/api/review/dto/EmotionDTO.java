package com.insidemovie.backend.api.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class EmotionDTO {
    private Double anger;
    private Double fear;
    private Double joy;
    private Double disgust;
    private Double sadness;
    private String repEmotion;
}
