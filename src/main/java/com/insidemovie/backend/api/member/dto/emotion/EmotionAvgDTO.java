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

    private Double anger;
    private Double fear;
    private Double joy;
    private Double disgust;
    private Double sadness;
    private EmotionType repEmotionType;

    public void setRepEmotionType(EmotionType repEmotionType) {
        this.repEmotionType = repEmotionType;
    }

    public EmotionAvgDTO(Double joy,
                         Double sadness,
                         Double anger,
                         Double fear,
                         Double disgust) {
        this.joy = joy;
        this.sadness = sadness;
        this.anger = anger;
        this.fear = fear;
        this.disgust = disgust;
        this.repEmotionType = EmotionType.NONE; // 혹은 null
    }
}