package com.insidemovie.backend.api.movie.entity;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "movie_emotion_summary")
public class MovieEmotionSummary {

    @Id
    private Long movieId;  // Movie의 PK와 일치

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "movie_id")
    private Movie movie;

    private Float joy;
    private Float sadness;
    private Float fear;
    private Float anger;
    private Float disgust;

    @Enumerated(EnumType.STRING)
    @Column(name = "dominant_emotion", length = 20)
    private EmotionType dominantEmotion; // 대표 감정

    public void updateFromDTO(EmotionAvgDTO dto) {
        this.joy = dto.getJoy().floatValue();
        this.sadness = dto.getSadness().floatValue();
        this.anger = dto.getAnger().floatValue();
        this.fear = dto.getFear().floatValue();
        this.disgust = dto.getDisgust().floatValue();
        this.dominantEmotion = dto.getRepEmotionType();
    }
}
