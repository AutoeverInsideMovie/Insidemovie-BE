package com.insidemovie.backend.api.movie.dto;

import com.insidemovie.backend.api.constant.EmotionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class MovieSearchResDto {
    private Long id;
    private String posterPath;
    private String title;
    private Double voteAverage;
    private LocalDate releaseDate;
    private EmotionType mainEmotion;
    private Double mainEmotionValue;
    private BigDecimal ratingAvg;
}
