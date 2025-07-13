package com.insidemovie.backend.api.friend.entity;

import com.insidemovie.backend.api.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Table(name = "friend")
@NoArgsConstructor
@AllArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long id;

    // 이 친구가 속한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "kakao_friend_id")
    private String kakaoFriendId;  // 카카오에서 제공하는 친구의 소셜 ID

    @Column(name = "friend_nickname")
    private String friendNickname;  // 카카오에서 가져온 친구의 닉네임
}
