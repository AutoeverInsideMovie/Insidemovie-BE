package com.insidemovie.backend.api.member.entity;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import jakarta.persistence.*;
import lombok.*;

import static org.apache.commons.lang3.math.NumberUtils.toFloat;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_emotion_summary")
public class MemberEmotionSummary {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private Float joy;
    private Float sadness;
    private Float fear;
    private Float anger;
    private Float neutral;

    @Enumerated(EnumType.STRING)
    private EmotionType repEmotionType; // 대표 감정

//    @Column(nullable = false)
//    private Float joy = 0.0f;
//
//    @Column(nullable = false)
//    private Float sadness = 0.0f;
//
//    @Column(nullable = false)
//    private Float anger = 0.0f;
//
//    @Column(nullable = false)
//    private Float fear = 0.0f;
//
//    @Column(nullable = false)
//    private Float neutral = 0.0f;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private EmotionType repEmotionType = EmotionType.NEUTRAL;

    // 평균 감정 정보를 DTO로부터 갱신
    public void updateFromDTO(EmotionAvgDTO dto) {
        this.joy            = dto.getJoy().floatValue();
        this.sadness        = dto.getSadness().floatValue();
        this.anger          = dto.getAnger().floatValue();
        this.fear           = dto.getFear().floatValue();
        this.neutral        = dto.getNeutral().floatValue();
        this.repEmotionType = dto.getRepEmotionType();
    }
}
