package com.insidemovie.backend.api.movie.entity;

import com.insidemovie.backend.api.constant.EmotionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "movie_emotion_summary")
public class MovieEmotionSummary {

    @Id
    private Long movieId;  // Movie의 PK와 일치

    @OneToOne
    @MapsId
    @JoinColumn(name = "movie_id")
    private Movie movie;

    private Float joy;
    private Float sadness;
    private Float fear;
    private Float anger;
    private Float neutral;

    @Enumerated(EnumType.STRING)
    private EmotionType dominantEmotion; // 대표 감정
}
