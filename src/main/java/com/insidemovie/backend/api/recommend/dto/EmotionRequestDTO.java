package com.insidemovie.backend.api.recommend.dto;

import lombok.Data;

@Data
public class EmotionRequestDTO {

    private double joy;
    private double anger;
    private double fear;
    private double disgust;
    private double sadness;
}
