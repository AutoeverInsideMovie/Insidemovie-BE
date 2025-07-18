package com.insidemovie.backend.api.member.repository;

import com.insidemovie.backend.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Query("UPDATE Member m SET m.refreshToken = NULL WHERE m.email = :email")
    int clearRefreshTokenByUserEmail(@Param("email") String email);

    // 일별 가입 수
    @Query(value = """
        SELECT DATE(created_at) AS date, COUNT(*) AS count
        FROM member
        WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        GROUP BY DATE(created_at)
        ORDER BY date ASC
        """, nativeQuery = true)
    List<Object[]> countMembersDaily();

    // 월별 가입 수
    @Query(value = """
        SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS count
        FROM member
        WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
        GROUP BY month
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> countMembersMonthly();
}
