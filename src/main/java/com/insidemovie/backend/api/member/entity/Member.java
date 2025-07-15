package com.insidemovie.backend.api.member.entity;

import com.insidemovie.backend.api.constant.Authority;
import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "member")
@AllArgsConstructor
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;
    private String password;
    private String nickname;

    private String socialType;  //  로그인한 소셜 타입의 식별자 값
    private String socialId;

    private Integer reportCount = 0;

    @Column(name = "main_emotion")
    @Enumerated(EnumType.STRING)
    private EmotionType mainEmotion;

    private String refreshToken;  // 리프레시 토큰

    // jwt
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Authority authority;


    @Builder
    public Member(String email, String password, String nickname, Authority authority, EmotionType mainEmotion) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.authority = authority;
        this.mainEmotion = mainEmotion;
    }

    // 리프레시 토큰 업데이트
    public void updateRefreshtoken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // 닉네임 변경
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 메인 감정 변경
    public void updateMainEmotion(EmotionType mainEmotion) {
        this.mainEmotion = mainEmotion;
    }

    // 비밀번호 변경
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }


}
