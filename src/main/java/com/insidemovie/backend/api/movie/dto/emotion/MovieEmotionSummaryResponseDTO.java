package com.insidemovie.backend.api.movie.dto.emotion;

import lombok.Data;

@Data
public class MovieEmotionSummaryResponseDTO {
    private Float joy;
    private Float anger;
    private Float sadness;
    private Float fear;
    private Float disgust;
    private String dominantEmotion;
}
