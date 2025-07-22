package com.insidemovie.backend.api.member.entity;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.dto.emotion.MemberEmotionSummaryRequestDTO;
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
    private Float disgust;

    @Enumerated(EnumType.STRING)
    @Column(name = "rep_emotion_type")
    private EmotionType repEmotionType; // 대표 감정

    public void setMember(Member member) {
        this.member = member;
    }

    // 평균 감정 정보를 DTO로부터 갱신
    public void updateFromDTO(EmotionAvgDTO dto) {
        this.joy            = dto.getJoy().floatValue();
        this.sadness        = dto.getSadness().floatValue();
        this.anger          = dto.getAnger().floatValue();
        this.fear           = dto.getFear().floatValue();
        this.disgust        = dto.getDisgust().floatValue();
        this.repEmotionType = dto.getRepEmotionType();
    }

    // 회원가입 시 사용자가 선택한 영화 기반 감정 데이터를 저장
    public void updateFromRequest(MemberEmotionSummaryRequestDTO dto, EmotionType rep) {
        this.joy = dto.getJoy();
        this.sadness = dto.getSadness();
        this.anger = dto.getAnger();
        this.fear = dto.getFear();
        this.disgust = dto.getDisgust();
        this.repEmotionType = rep;
    }
}
