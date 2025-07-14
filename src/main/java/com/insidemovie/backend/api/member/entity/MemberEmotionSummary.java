package com.insidemovie.backend.api.member.entity;

import com.insidemovie.backend.api.constant.EmotionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_emotion_summary")
public class MemberEmotionSummary {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private float avgJoy;
    private float avgSadness;
    private float avgAnger;
    private float avgFear;
    private float avgDisgust;

    @Enumerated(EnumType.STRING)
    private EmotionType repEmotionType;  // 대표 감정
}
