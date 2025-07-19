package com.insidemovie.backend.api.member.repository;

import com.insidemovie.backend.api.member.entity.Member;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
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

    // 누적 통계 특정 시점까지 전체 회원 수 (하루 단위 누적, 월 단위 누적 공통 사용)
    long countByCreatedAtLessThan(LocalDateTime dateTime);

    // 일별 가입 수
    @Query("""
        SELECT DATE(m.createdAt), COUNT(m)
        FROM Member m
        WHERE m.createdAt >= :start AND m.createdAt < :end
        GROUP BY DATE(m.createdAt)
        ORDER BY DATE(m.createdAt)
    """)
    List<Object[]> countMembersDaily(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    // 월별 가입 수
    @Query("""
        SELECT FUNCTION('DATE_FORMAT', m.createdAt, '%Y-%m'), COUNT(m)
        FROM Member m
        WHERE m.createdAt >= :start AND m.createdAt < :end
        GROUP BY FUNCTION('DATE_FORMAT', m.createdAt, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', m.createdAt, '%Y-%m')
    """)
    List<Object[]> countMembersMonthly(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}
