package com.insidemovie.backend.api.member.dto.emotion;

import com.insidemovie.backend.api.constant.EmotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionAvgDTO {

    @Builder.Default
    private Double joy     = 0.0;
    @Builder.Default
    private Double sadness = 0.0;
    @Builder.Default
    private Double anger   = 0.0;
    @Builder.Default
    private Double fear    = 0.0;
    @Builder.Default
    private Double disgust = 0.0;

    @Builder.Default
    private EmotionType repEmotionType = EmotionType.DISGUST;

    public EmotionAvgDTO(Double joy,
                         Double sadness,
                         Double anger,
                         Double fear,
                         Double disgust) {
        this.joy             = joy != null ? joy : 0.0;
        this.sadness         = sadness != null ? sadness : 0.0;
        this.anger           = anger != null ? anger : 0.0;
        this.fear            = fear != null ? fear : 0.0;
        this.disgust         = disgust != null ? disgust : 0.0;
        this.repEmotionType  = EmotionType.DISGUST;
    }

    public void setRepEmotionType(EmotionType repEmotionType) {
        this.repEmotionType = repEmotionType;
    }
}