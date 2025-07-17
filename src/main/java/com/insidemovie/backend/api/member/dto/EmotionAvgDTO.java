package com.insidemovie.backend.api.member.dto;

import com.insidemovie.backend.api.constant.EmotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

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
    private Double neutral = 0.0;

    @Builder.Default
    private EmotionType repEmotionType = EmotionType.NEUTRAL;

    public EmotionAvgDTO(Double joy,
                         Double sadness,
                         Double anger,
                         Double fear,
                         Double neutral) {
        this.joy             = joy != null ? joy : 0.0;
        this.sadness         = sadness != null ? sadness : 0.0;
        this.anger           = anger != null ? anger : 0.0;
        this.fear            = fear != null ? fear : 0.0;
        this.neutral         = neutral != null ? neutral : 0.0;
        this.repEmotionType  = EmotionType.NEUTRAL;
    }

    public void setRepEmotionType(EmotionType repEmotionType) {
        this.repEmotionType = repEmotionType;
    }
}