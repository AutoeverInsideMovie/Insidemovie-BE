package com.insidemovie.backend.api.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Map;

@Getter
@NoArgsConstructor

public class PredictResponseDTO {
    private String text;
    private Map<String, Double> probabilities;
    private Instant instant;
}
