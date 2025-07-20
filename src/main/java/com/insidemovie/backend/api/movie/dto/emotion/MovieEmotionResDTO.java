package com.insidemovie.backend.api.movie.dto.emotion;

import com.insidemovie.backend.api.constant.EmotionType;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class MovieEmotionResDTO {
    private Float joy;
    private Float anger;
    private Float sadness;
    private Float fear;
    private Float disgust;
    private EmotionType dominantEmotion;
}
