package com.insidemovie.backend.api.member.entity;

import com.insidemovie.backend.api.constant.Authority;
import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.report.entity.Report;
import com.insidemovie.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
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
    @Column(name = "report_count", nullable = true)
    private Integer reportCount = 0;

    // 사용자가 신고한 내역
    @Builder.Default
    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Report> reportsFiled = new ArrayList<>();

    // 사용자가 신고당한 내역
    @Builder.Default
    @OneToMany(mappedBy = "reportedMember", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Report> reportsReceived = new ArrayList<>();

    // 감정 요약
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private MemberEmotionSummary emotionSummary;

    @Column(name="refresh_token")
    private String refreshToken;  // 리프레시 토큰

    // jwt
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Authority authority;

    @Column(nullable = true)
    @Builder.Default
    private Boolean isBanned = false;  // 정지

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

    // 닉네임 변경
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 비밀번호 변경
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    // 회원 정지
    public void setBanned(boolean banned) {
        this.isBanned = banned;
    }

    public boolean isBanned() {
        return this.isBanned;
    }

    // 신고 횟수 증가
    public void incrementReportCount() {
        this.reportCount++;
    }
}
