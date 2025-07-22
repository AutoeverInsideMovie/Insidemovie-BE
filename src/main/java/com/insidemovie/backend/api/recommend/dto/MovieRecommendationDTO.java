package com.insidemovie.backend.api.recommend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insidemovie.backend.api.constant.EmotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class MovieRecommendationDTO {

    private Long movieId;
    private String title;
    private String posterPath;
    private double voteAverage;

    private EmotionType dominantEmotion;  // 대표 감정
    private double dominantEmotionRatio;  // 대표 감정 퍼센트

    @JsonIgnore
    private double similarity;  // 유사도

    private BigDecimal ratingAvg;
}
