package com.insidemovie.backend.api.member.repository;

import com.insidemovie.backend.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findBySocialId(String socialId);

    Optional<Member> findByRefreshToken(String refreshToken);

    boolean existsByNickname(String nickname);

    // 이메일 또는 닉네임에 키워드 포함된 회원을 페이징 조회
    Page<Member> findByEmailContainingOrNicknameContaining(String email, String nickname, Pageable pageable);

    // 정지된 회원 수
    long countByIsBannedTrue();

    @Modifying
    @Query("UPDATE Member m SET m.refreshToken = NULL WHERE m.email = :email")
    int clearRefreshTokenByUserEmail(@Param("email") String email);
}
