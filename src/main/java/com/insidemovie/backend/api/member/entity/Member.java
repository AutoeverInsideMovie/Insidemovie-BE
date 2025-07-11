package com.insidemovie.backend.api.member.entity;

import com.insidemovie.backend.api.constant.Authority;
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

    @Builder.Default
    private Integer reportCount = 0;

    private String refreshToken;  // 리프레시 토큰

    // jwt
    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Builder
    public Member(String email, String password, String nickname, Authority authority) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.authority = authority;
    }

    // 리프레시 토큰 업데이트
    public void updateRefreshtoken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
